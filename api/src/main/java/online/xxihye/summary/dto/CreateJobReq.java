package online.xxihye.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Job 생성 DTO")
public record CreateJobReq(
    @Schema(description = "요약할 원문 텍스트")
    @NotBlank
    String text
) {
}