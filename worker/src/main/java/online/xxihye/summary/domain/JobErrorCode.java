package online.xxihye.summary.domain;

public enum JobErrorCode {
    INVALID_PAYLOAD(false),
    DB_FAILED(false),
    NOT_FOUND_INPUT(false),
    INVALID_INPUT_TEXT(false),
    UNKNOWN(false),
    RETRY_EXCEEDED(false),
    AI_BAD_REQUEST(false),
    AI_UNAUTHORIZED(false),
    AI_PERMISSION_DENIED(false),
    AI_NOT_FOUND(false),
    AI_RATE_LIMITED(true),
    AI_INTERNAL_ERROR(true),
    AI_SERVICE_UNAVAILABLE(true),
    AI_TIMEOUT(true),
    AI_UNKNOWN_ERROR(false),
    AI_NETWORK_ERROR(true);

    private final boolean retryable;

    JobErrorCode(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable(){
        return retryable;
    }
}
