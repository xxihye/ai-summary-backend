package online.xxihye.summary.dto;

import online.xxihye.summary.domain.JobStatus;

public class CreateJobRes {

    private final Long jobId;
    private final String status;

    public CreateJobRes(Long jobId, JobStatus status) {
        this.jobId = jobId;
        this.status = getStatus();
    }

    public Long getJobId() { return jobId; }
    public String getStatus() { return status; }
}
