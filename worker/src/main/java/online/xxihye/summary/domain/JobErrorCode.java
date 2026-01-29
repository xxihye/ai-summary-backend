package online.xxihye.summary.domain;

public enum JobErrorCode {
    INVALID_PAYLOAD,
    DB_FAILED,
    NOT_FOUND_INPUT,
    INVALID_INPUT_TEXT,
    UNKNOWN,
    RETRY_EXCEEDED,
    AI_BAD_REQUEST,          // 400
    AI_UNAUTHORIZED,         // 401
    AI_PERMISSION_DENIED,    // 403
    AI_NOT_FOUND,            // 404
    AI_RATE_LIMITED,         // 429
    AI_INTERNAL_ERROR,       // 500
    AI_SERVICE_UNAVAILABLE,  // 503
    AI_TIMEOUT,              // 504
    AI_UNKNOWN_ERROR,        // 그 외
    AI_NETWORK_ERROR         // GenAiIOException 등
}
