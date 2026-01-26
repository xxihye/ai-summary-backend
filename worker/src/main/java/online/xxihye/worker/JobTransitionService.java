package online.xxihye.worker;

import lombok.RequiredArgsConstructor;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobTransitionService {

    private final SummarizationJobRepository repository;

    @Transactional
    public int moveToRunning(Long jobId, LocalDateTime now) {
        return repository.moveToRunningIfPending(
            jobId, JobStatus.PENDING, JobStatus.RUNNING, now, now
        );
    }

    @Transactional
    public int markSuccess(Long jobId, String resultText, String model, LocalDateTime now) {
        return repository.markSuccessIfRunning(
            jobId, JobStatus.RUNNING, JobStatus.SUCCESS, resultText, model, now, now
        );
    }

    @Transactional
    public int markFailed(Long jobId, JobErrorCode code, String msg, String model, LocalDateTime now) {
        return repository.markFailedIfPendingOrRunning(
            jobId,
            java.util.List.of(JobStatus.PENDING, JobStatus.RUNNING),
            JobStatus.FAILED,
            code, msg, model,
            now, now
        );
    }
}
