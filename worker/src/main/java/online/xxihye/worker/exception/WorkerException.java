package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public abstract class WorkerException extends RuntimeException{

    private final JobErrorCode errorCode;

    protected WorkerException(JobErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    protected WorkerException(JobErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected WorkerException(JobErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public JobErrorCode getErrorCode(){
        return errorCode;
    }
}
