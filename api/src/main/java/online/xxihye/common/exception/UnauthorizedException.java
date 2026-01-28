package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode code) {
        super(HttpStatus.UNAUTHORIZED, code);
    }
}