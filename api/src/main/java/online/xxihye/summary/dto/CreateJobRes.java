package online.xxihye.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job 생성 응답 DTO")
public record CreateJobRes(
    @Schema(description = "생성된 Job ID")
    Long jobId,

    @Schema(description = "Job 상태")
    String status
) {
}