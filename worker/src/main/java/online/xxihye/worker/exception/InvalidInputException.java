package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public class InvalidInputException extends WorkerException{
    public InvalidInputException() {
        super(JobErrorCode.INVALID_INPUT_TEXT, "invalid input text");
    }
}
