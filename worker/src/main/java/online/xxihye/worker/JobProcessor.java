package online.xxihye.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.infra.gemini.AiErrorPatterns;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.repository.SummarizationJobRepository;
import online.xxihye.summary.summarizer.Summarizer;
import online.xxihye.worker.exception.AiProcessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobProcessor {

    private final SummarizationJobRepository repository;
    private final JobTransitionService transitionService;
    private final Summarizer summarizer;

    public void process(Long jobId) {

        int running = transitionService.moveToRunning(jobId, LocalDateTime.now());

        if (running == 0) {
            repository.findById(jobId)
                      .ifPresentOrElse(
                          j -> log.info("skip processing due to status. jobId={}, status={}", jobId, j.getStatus()),
                          () -> log.warn("job not found. jobId={}", jobId)
                      );
            return;
        }

        //원문 조회
        String inputText = repository.findById(jobId)
                                     .map(SummarizationJob::getInputText)
                                     .orElseThrow();

        try {
            String summary = summarizer.summarize(inputText);
            int succeeded = transitionService.markSuccess(
                jobId,
                summary,
                summarizer.getModelName(),
                LocalDateTime.now()
            );
            log.info("job success. jobId={}, updated={}", jobId, succeeded);

        } catch (Exception e) {
            JobErrorCode errorCode = mapAiError(e);
            String msg = safeMessage(e);

            int failed = transitionService.markFailed(
                jobId,
                errorCode,
                msg,
                summarizer.getModelName(),
                LocalDateTime.now()
            );
            log.info("job failed. jobId={}, errorCode={}, updated={}", jobId, errorCode, failed);

            throw new AiProcessException(errorCode, msg, e);
        }
    }

    //LLM 모델 ai로 처리중 오류 분류
    private JobErrorCode mapAiError(Exception e) {
        String message = normalize(e.getMessage());

        if (containsAny(message, AiErrorPatterns.RATE_LIMIT)) {
            return JobErrorCode.AI_RATE_LIMITED;
        }
        if (containsAny(message, AiErrorPatterns.TIMEOUT)) {
            return JobErrorCode.AI_TIMEOUT;
        }
        if (containsAny(message, AiErrorPatterns.UNAVAILABLE)) {
            return JobErrorCode.AI_UNAVAILABLE;
        }
        if (containsAny(message, AiErrorPatterns.BAD_REQUEST)) {
            return JobErrorCode.AI_BAD_REQUEST;
        }
        return JobErrorCode.UNKNOWN;
    }

    private String normalize(String msg) {
        return msg == null ? "" : msg.toLowerCase();
    }

    private boolean containsAny(String target, List<String> patterns) {
        return patterns.stream()
                       .anyMatch(target::contains);
    }

    private String safeMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage()
                                       .isBlank()) {
            return e.getClass()
                    .getSimpleName();
        }
        return e.getMessage()
                .length() > 300
            ? e.getMessage()
               .substring(0, 300)
            : e.getMessage();
    }
}
