package online.xxihye.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobProcessor {

    private final SummarizationJobRepository repository;

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
            String summary = "SUMMARY: " + job.getInputTextLen() + " chars";
            job.markSuccess(summary);

            log.info("job success. jobId={}", jobId);
        } catch (Exception e) {
            String msg = e.getMessage();

            if (msg != null && !msg.isBlank()) {
                e.getClass().getSimpleName();
            }

            job.markFailed(JobErrorCode.SUMMARY_FAILED, msg);
            log.error("job failed. jobId={}, errorCode={}, message={}", jobId, JobErrorCode.SUMMARY_FAILED, msg, e);
        }
    }
}
