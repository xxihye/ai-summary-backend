package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public class AiProcessException extends RuntimeException {

    private final JobErrorCode errorCode;

    public AiProcessException(JobErrorCode errorCode, String message, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }

    public JobErrorCode getErrorCode(){
        return errorCode;
    }
}
