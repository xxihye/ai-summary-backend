package online.xxihye.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.infra.genai.AiErrorPatterns;
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
    private final Summarizer summarizer;

    @Transactional
    public void process(Long jobId) {
        LocalDateTime now = LocalDateTime.now();

        int updated = repository.moveToRunningIfPending(
            jobId,
            JobStatus.PENDING,
            JobStatus.RUNNING,
            now,
            now
        );

        if (updated == 0) {
            //해당 jobId를 가진 job db 조회
            Optional<SummarizationJob> opt = repository.findById(jobId);

            //Stream 메시지와 DB 오류
            if (opt.isEmpty()) {
                log.warn("job not found. jobId={}", jobId);
                return;
            }

            //다른 컨슈머가 처리하는 걸로 판단
            JobStatus current = opt.get().getStatus();
            log.info("skip processing due to status. jobId={}, status={}", jobId, current);
            return;
        }

        SummarizationJob job = repository.findById(jobId)
                                         .orElseThrow();

        try {
            String summary = summarizer.summarize(job.getInputText());
            String usedModel = summarizer.getModelName();
            job.markSuccess(summary, usedModel);

            log.info("job success. jobId={}", jobId);
        } catch (Exception e) {
            JobErrorCode errorCode = mapAiError(e);
            String msg = safeMessage(e);

            job.markFailed(errorCode, msg, summarizer.getModelName());
            log.error("job failed. jobId={}, errorCode={}, message={}", jobId, errorCode, msg, e);

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
        return patterns.stream().anyMatch(target::contains);
    }

    private String safeMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getMessage().length() > 300
            ? e.getMessage().substring(0, 300)
            : e.getMessage();
    }
}
