package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(ErrorCode code) {
        super(HttpStatus.CONFLICT, code);
    }
}