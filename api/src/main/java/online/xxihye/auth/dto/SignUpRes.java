package online.xxihye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답 DTO")
public record SignUpRes(
    @Schema(description = "회원 번호")
    Long userNo,
    @Schema(description = "회원 아이디")
    String userId
) {}
