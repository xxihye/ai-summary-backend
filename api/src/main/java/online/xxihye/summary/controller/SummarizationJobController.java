package online.xxihye.summary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.xxihye.summary.dto.CreateJobReq;
import online.xxihye.summary.dto.CreateJobRes;
import online.xxihye.summary.service.SummarizationJobService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/summaries/jobs")
public class SummarizationJobController {

    private final SummarizationJobService service;

    @PostMapping
    public CreateJobRes create(@Valid @RequestBody CreateJobReq req){
        return service.createJob(req);
    }
}
