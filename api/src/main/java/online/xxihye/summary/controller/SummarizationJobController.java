package online.xxihye.summary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Jobs", description = "요약 Job 생성/조회 API")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/summaries/jobs")
public class SummarizationJobController {

    private final SummarizationJobService service;

    @Operation(
        summary = "요약 작업 생성",
        description = """
            텍스트 요약을 위한 Job 생성
                        
            - 동일한 입력이 이미 처리되어 결과가 존재하면 cache hit로 즉시 SUCCESS 상태의 Job을 생성
            - 결과가 없으면 PENDING 상태 Job 생성 후 Worker 처리 대상으로 enqueue
            - 일일 쿼터 정책(일 10회) 적용
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job 생성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "429", description = "일일 사용량 초과")
    })
    @PostMapping
    public ResponseEntity<CreateJobRes> createJob(@AuthenticationPrincipal Long userNo,
                                                  @RequestBody CreateJobReq req) {
        return ResponseEntity.ok(service.createJob(userNo, req));
    }

    @Operation(
        summary = "Job 상세 조회",
        description = "Job 상태 및 메타정보 조회"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "Job 없음")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<JobDetailRes> getJob(@AuthenticationPrincipal Long userNo,
                                               @Parameter(description = "Job ID") @PathVariable Long jobId) {
        return ResponseEntity.ok(service.getJob(userNo, jobId));
    }

    @Operation(
        summary = "요약 결과 조회",
        description = "성공한 상태의 Job 요약 결과를 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "409", description = "Job이 아직 완료되지 않음"),
        @ApiResponse(responseCode = "404", description = "Job 없음")
    })
    @GetMapping("/{jobId}/result")
    public ResponseEntity<JobResultRes> getResult(@AuthenticationPrincipal Long userNo,
                                                  @Parameter(description = "Job ID") @PathVariable Long jobId) {
        return ResponseEntity.ok(service.getResult(userNo, jobId));
    }
}
