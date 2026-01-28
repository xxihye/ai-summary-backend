package online.xxihye.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handle(BusinessException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(new ErrorRes(e.getErrorCode().name()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorRes(ErrorCode.INTERNAL_SERVER_ERROR.name()));
    }

    public record ErrorRes(String code) {}
}
