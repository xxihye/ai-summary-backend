package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    protected BusinessException(HttpStatus status, ErrorCode errorCode) {
        super(errorCode.name());
        this.status = status;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus(){
        return status;
    }
}



