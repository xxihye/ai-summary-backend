package online.xxihye.summary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;

import java.time.LocalDateTime;

@Schema(description = "Job 상세 조회 응답 DTO")
public record JobDetailRes(
    @Schema(description = "Job ID")
    Long jobId,

    @Schema(description = "Job 상태")
    JobStatus status,

    @Schema(description = "원문 텍스트")
    String inputText,

    @Schema(description = "생성일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "시작일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startedAt,

    @Schema(description = "종료일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime finishedAt,

    @Schema(description = "결과 여부")
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


