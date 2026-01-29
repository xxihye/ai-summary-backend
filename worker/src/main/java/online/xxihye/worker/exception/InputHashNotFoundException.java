package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public class InputHashNotFoundException extends WorkerException{

    public InputHashNotFoundException() {
        super(JobErrorCode.INVALID_INPUT_TEXT, "input hash not found");
    }
}
