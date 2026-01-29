package online.xxihye.worker;

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.SummarizationInput;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.repository.SummarizationInputRepository;
import online.xxihye.summary.repository.SummarizationJobRepository;
import online.xxihye.summary.service.SummaryService;
import online.xxihye.summary.summarizer.Summarizer;
import online.xxihye.worker.exception.AiProcessException;
import online.xxihye.worker.exception.InputHashNotFoundException;
import online.xxihye.worker.exception.InputNotFoundException;
import online.xxihye.worker.exception.WorkerException;
import online.xxihye.worker.service.JobTransitionService;
import org.hibernate.JDBCException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobProcessor {

    private static final int MAX_LENGTH = 300;

    private final SummarizationJobRepository jobRepository;
    private final SummarizationInputRepository inputRepository;
    private final JobTransitionService transitionService;
    private final SummaryService summaryService;
    private final Summarizer summarizer;

    public void process(Long jobId) {
        int running = transitionService.moveToRunning(jobId, LocalDateTime.now());

        if (running == 0) {
            jobRepository.findById(jobId)
                         .ifPresentOrElse(
                             j -> log.info("skip processing due to status. jobId={}, status={}", jobId, j.getStatus()),
                             () -> log.warn("job not found. jobId={}", jobId)
                         );
            return;
        }

        try {
            //원문 조회
            String inputText = inputRepository.findById(jobId)
                                              .map(SummarizationInput::getInputText)
                                              .orElseThrow(InputNotFoundException::new);

            //hash 조회
            String inputHash = jobRepository.findById(jobId)
                                            .map(SummarizationJob::getInputHash)
                                            .orElseThrow(InputHashNotFoundException::new);

            //요약 수행
            String summaryText = summarizer.summarize(inputText);

            //요약 결과
            Long resultId = summaryService.saveSummary(inputHash, summaryText)
                                          .getId();

            //성공 결과 저장
            int succeeded = transitionService.markSuccess(
                jobId,
                resultId,
                summarizer.getModelName(),
                LocalDateTime.now()
            );
            log.info("job success. jobId={}, updated={}", jobId, succeeded);

        } catch (WorkerException e) {
            transitionService.markFailed(
                jobId,
                e.getErrorCode(),
                safeMessage(e),
                summarizer.getModelName(),
                LocalDateTime.now()
            );

            log.info("job failed. jobId={}, errorCode={}, updated={}", jobId, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            int failed = transitionService.markFailed(
                jobId,
                mapError(e),
                safeMessage(e),
                summarizer.getModelName(),
                LocalDateTime.now()
            );

            log.info("job failed. jobId={}, errorCode={}, updated={}", jobId, e.getMessage(), failed);
            throw e;
        }
    }

    //처리중 오류 분류
    private JobErrorCode mapError(Exception e) {
        // DB 관련 예외
        if (isDbException(e)) {
            return JobErrorCode.DB_FAILED;
        }

        return JobErrorCode.UNKNOWN;
    }

    private boolean isDbException(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof DataAccessException
                || t instanceof PersistenceException
                || t instanceof JDBCException
                || t instanceof java.sql.SQLException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
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
                .length() > MAX_LENGTH ? e.getMessage()
                                          .substring(0, MAX_LENGTH) : e.getMessage();
    }
}
