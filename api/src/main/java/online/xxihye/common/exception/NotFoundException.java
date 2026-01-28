package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode code) {
        super(HttpStatus.NOT_FOUND, code);
    }
}