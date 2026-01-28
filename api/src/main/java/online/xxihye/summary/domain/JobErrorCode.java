package online.xxihye.summary.domain;

public enum JobErrorCode {
    SUMMARY_FAILED,
    INVALID_PAYLOAD,
    DB_UPDATE_FAILED,
    NOT_FOUND_INPUT,
    UNKNOWN,
    RETRY_EXCEEDED,
    AI_RATE_LIMITED,
    AI_TIMEOUT,
    AI_UNAVAILABLE,
    AI_BAD_REQUEST
}
