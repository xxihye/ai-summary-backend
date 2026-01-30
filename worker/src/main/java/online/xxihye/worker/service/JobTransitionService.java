package online.xxihye.worker.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.summary.domain.JobErrorCode;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobTransitionService {

    private final SummarizationJobRepository repository;

    @Transactional
    public int moveToRunning(Long jobId, LocalDateTime now) {
        return repository.moveToRunning(
            jobId,
            List.of(JobStatus.PENDING, JobStatus.RETRYING),
            JobStatus.RUNNING,
            now
        );
    }

    @Transactional
    public int markSuccess(Long jobId, Long resultId, String model, LocalDateTime now) {
        return repository.markSuccessIfRunning(
            jobId,
            JobStatus.RUNNING,
            JobStatus.SUCCESS,
            resultId,
            model,
            now,
            now
        );
    }

    @Transactional
    public int markFailed(Long jobId, JobErrorCode code, String msg, String model, LocalDateTime now) {
        return repository.markFailedIfPendingOrRunning(
            jobId,
            List.of(JobStatus.PENDING, JobStatus.RUNNING),
            JobStatus.FAILED,
            code,
            msg,
            model,
            now,
            now
        );
    }

    @Transactional
    public int markRetrying(Long jobId, JobErrorCode errorCode, String msg, LocalDateTime now){
        return repository.markRetrying(
            jobId,
            JobStatus.RUNNING,
            JobStatus.RETRYING,
            errorCode,
            msg,
            now
        );
    }
}
