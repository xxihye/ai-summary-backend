package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public class AiProcessException extends WorkerException {

    public AiProcessException(JobErrorCode errorCode) {
        super(errorCode);
    }

    public AiProcessException(JobErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AiProcessException(JobErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }


}
