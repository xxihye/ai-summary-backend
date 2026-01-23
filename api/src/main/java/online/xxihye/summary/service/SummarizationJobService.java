package online.xxihye.summary.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.common.util.HashUtil;
import online.xxihye.infra.redis.JobQueueClient;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.dto.CreateJobReq;
import online.xxihye.summary.dto.CreateJobRes;
import online.xxihye.summary.repository.SummarizationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String model = (req.getModel() == null || req.getModel().isBlank()) ? "dummy-model" : req.getModel();

        String inputHash = HashUtil.sha256(text);

        SummarizationJob job = new SummarizationJob(userId, inputHash, text.length(), model);
        repository.save(job);

        queue.enqueue(job.getId());

        return new CreateJobRes(job.getId(), job.getStatus());
    }
}
