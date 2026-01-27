package online.xxihye.summary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;

import java.time.LocalDateTime;

public record JobDetailRes(
    Long jobId,
    JobStatus status,
    String inputText,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startedAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime finishedAt,
    boolean hasResult
) {
    public static JobDetailRes from(SummarizationJob job, String inputText) {
        return new JobDetailRes(
            job.getId(),
            job.getStatus(),
            inputText,
            job.getCreatedAt(),
            job.getStartedAt(),
            job.getFinishedAt(),
            !(job.getResultId() == null)
        );
    }
}


