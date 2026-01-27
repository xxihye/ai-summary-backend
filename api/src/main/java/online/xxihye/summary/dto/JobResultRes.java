package online.xxihye.summary.dto;

public record JobResultRes(
    Long jobId,
    String resultText,
    String model,
    String promptVersion,
    boolean cacheHit
) {
    public static JobResultRes of(Long jobId, String resultText, String model, String promptVersion, boolean cacheHit){
        return new JobResultRes(jobId, resultText, model, promptVersion, cacheHit);
    }
}
