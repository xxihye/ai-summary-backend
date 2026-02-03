package online.xxihye.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job 요약 결과 응답 DTO")
public record JobResultRes(
    @Schema(description = "Job ID")
    Long jobId,

    @Schema(description = "요약 결과")
    String resultText,

    @Schema(description = "AI 모델")
    String model,

    @Schema(description = "프롬프트 버전")
    String promptVersion,

    @Schema(description = "캐시 히트 여부")
    boolean cacheHit
) {
    public static JobResultRes of(Long jobId, String resultText, String model, String promptVersion, boolean cacheHit){
        return new JobResultRes(jobId, resultText, model, promptVersion, cacheHit);
    }
}
