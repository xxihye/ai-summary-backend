package online.xxihye.summary.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.common.util.HashUtil;
import online.xxihye.infra.redis.JobQueueClient;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.dto.CreateJobReq;
import online.xxihye.summary.dto.CreateJobRes;
import online.xxihye.summary.dto.JobDetailRes;
import online.xxihye.summary.dto.JobResultRes;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class SummarizationJobService {

    private final SummarizationJobRepository repository;
    private final JobQueueClient queue;

    @Transactional
    public CreateJobRes createJob(CreateJobReq req) {
        //todo : 지금은 하드코딩
        Long userId = 1L;
        String text = req.getText().trim();
        String inputHash = HashUtil.sha256(text);

        SummarizationJob job = new SummarizationJob(userId, text, inputHash, text.length());
        repository.save(job);

        queue.enqueue(job.getId());

        return new CreateJobRes(job.getId(), job.getStatus());
    }

    public JobDetailRes getJob(Long jobId) {
        SummarizationJob job = repository.findById(jobId)
                                         .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "job not found"));

        return JobDetailRes.from(job);
    }

    public JobResultRes getResult(Long jobId) {
        SummarizationJob job = repository.findById(jobId)
                                         .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "job not found"));

        //성공한 경우가 아닌 경우, 예외 처리
        if (job.getStatus() != JobStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "job is not finished");
        }

        //요약 결과 빈값인 경우, 예외처리
        if (job.getResultText() == null || job.getResultText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "result is empty");
        }

        return JobResultRes.of(job.getId(), job.getResultText());
    }
}
