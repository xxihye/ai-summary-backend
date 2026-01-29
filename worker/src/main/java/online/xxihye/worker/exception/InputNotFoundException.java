package online.xxihye.worker.exception;

import online.xxihye.summary.domain.JobErrorCode;

public class InputNotFoundException extends WorkerException{

    public InputNotFoundException() {
        super(JobErrorCode.NOT_FOUND_INPUT, "input text not found");
    }

}
