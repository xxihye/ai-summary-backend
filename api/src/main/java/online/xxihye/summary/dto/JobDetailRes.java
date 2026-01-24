package online.xxihye.summary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;

import java.time.LocalDateTime;

public record JobDetailRes(
    Long jobId,
    JobStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startedAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime finishedAt,
    boolean hasResult
) {
    public static JobDetailRes from(SummarizationJob job){
        return new JobDetailRes(
            job.getId(),
            job.getStatus(),
            job.getCreatedAt(),
            job.getStartedAt(),
            job.getFinishedAt(),
            job.getResultText() != null && !job.getResultText().isBlank()
        );
    }
}


