package online.xxihye.summary.dto;

public record JobResultRes(
    Long jobId,
    String resultText
) {
    public static JobResultRes of(Long jobId, String resultText){
        return new JobResultRes(jobId, resultText);
    }
}
