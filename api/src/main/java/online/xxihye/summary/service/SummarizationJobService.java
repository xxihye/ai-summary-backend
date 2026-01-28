package online.xxihye.summary.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.common.exception.ConflictException;
import online.xxihye.common.exception.ErrorCode;
import online.xxihye.common.exception.NotFoundException;
import online.xxihye.common.util.HashUtil;
import online.xxihye.infra.redis.JobQueueClient;
import online.xxihye.summary.domain.JobStatus;
import online.xxihye.summary.domain.SummarizationInput;
import online.xxihye.summary.domain.SummarizationJob;
import online.xxihye.summary.domain.Summary;
import online.xxihye.summary.dto.CreateJobReq;
import online.xxihye.summary.dto.CreateJobRes;
import online.xxihye.summary.dto.JobDetailRes;
import online.xxihye.summary.dto.JobResultRes;
import online.xxihye.summary.repository.SummarizationInputRepository;
import online.xxihye.summary.repository.SummarizationJobRepository;
import online.xxihye.summary.repository.SummaryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class SummarizationJobService {

    private static final String PROMPT_VERSION = "v1";

    private final SummarizationJobRepository jobRepository;
    private final SummarizationInputRepository inputRepository;
    private final SummaryRepository summaryRepository;
    private final JobQueueClient queue;

    //job 생성
    @Transactional
    public CreateJobRes createJob(Long userNo, CreateJobReq req) {
        String inputText = req.getText().trim();
        String inputHash = HashUtil.sha256(inputText);
        int inputLen = inputText.length();

        //job 생성
        SummarizationJob job = SummarizationJob.createPending(
            userNo,
            inputHash,
            inputLen,
            PROMPT_VERSION
        );
        SummarizationJob savedJob = jobRepository.save(job);

        // input 저장
        inputRepository.save(new SummarizationInput(savedJob.getId(), inputText));

        //레디스에 저장
        queue.enqueue(job.getId());

        return new CreateJobRes(job.getId(), JobStatus.PENDING);
    }

    //결과 조회
    public JobDetailRes getJob(Long userNo, Long jobId) {
        SummarizationJob job = jobRepository.findByIdAndUserNo(jobId, userNo)
                                            .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_NOT_FOUND));

        String input = inputRepository.findByJobId(jobId)
                                      .map(SummarizationInput::getInputText)
                                      .orElse(null);

        return JobDetailRes.from(job, input);
    }

    public JobResultRes getResult(Long userNo, Long jobId) {
        SummarizationJob job = jobRepository.findByIdAndUserNo(jobId, userNo)
                                            .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_NOT_FOUND));

        //성공한 경우가 아닌 경우, 예외 처리
        if (job.getStatus() != JobStatus.SUCCESS) {
            throw new ConflictException(ErrorCode.JOB_NOT_COMPLETED);
        }

        String result = summaryRepository.findById(job.getResultId())
                                         .map(Summary::getSummaryText)
                                         .orElse(null);

        return JobResultRes.of(jobId, result, job.getModel(), job.getPromptVersion(), job.getCacheHit());
    }

}
