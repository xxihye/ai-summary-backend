package online.xxihye.worker;

import lombok.RequiredArgsConstructor;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
            now);

        if (updated == 0) {
            return;
        }

        SummarizationJob job = repository.findById(jobId)
                                         .orElseThrow();

        String summary = "SUMMARY: " + job.getInputTextLen() + " chars";
        job.markSuccess(summary, LocalDateTime.now());
    }
}
