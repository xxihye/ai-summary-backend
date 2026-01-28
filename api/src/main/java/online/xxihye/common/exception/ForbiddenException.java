package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode code) {
        super(HttpStatus.FORBIDDEN, code);
    }
}