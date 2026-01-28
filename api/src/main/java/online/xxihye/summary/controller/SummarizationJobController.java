package online.xxihye.summary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.xxihye.summary.dto.CreateJobReq;
import online.xxihye.summary.dto.CreateJobRes;
import online.xxihye.summary.dto.JobDetailRes;
import online.xxihye.summary.dto.JobResultRes;
import online.xxihye.summary.service.SummarizationJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/summaries/jobs")
public class SummarizationJobController {

    private final SummarizationJobService service;

    @Operation(description = "요약 작업 생성")
    @PostMapping
    public ResponseEntity<CreateJobRes> createJob(@AuthenticationPrincipal Long userNo,
                                                  @RequestBody CreateJobReq req) {
        return ResponseEntity.ok(service.createJob(userNo, req));
    }

    @Operation(description = "작업 상태 조회")
    @GetMapping("/{jobId}")
    public ResponseEntity<JobDetailRes> getJob(@AuthenticationPrincipal Long userNo,
                                               @PathVariable Long jobId) {
        return ResponseEntity.ok(service.getJob(userNo, jobId));
    }

    @Operation(description = "작업 결과 조회")
    @GetMapping("/{jobId}/result")
    public ResponseEntity<JobResultRes> getResult(@AuthenticationPrincipal Long userNo,
                                                  @PathVariable Long jobId) {
        return ResponseEntity.ok(service.getResult(userNo, jobId));
    }
}
