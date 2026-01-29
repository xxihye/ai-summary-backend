package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public class DailyQuotaExceededException extends BusinessException{
    public DailyQuotaExceededException(ErrorCode errorCode) {
        super(HttpStatus.TOO_MANY_REQUESTS, errorCode);
    }
}
