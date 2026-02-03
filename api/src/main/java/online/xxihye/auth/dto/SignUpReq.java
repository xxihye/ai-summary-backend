package online.xxihye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청 DTO")
public record SignUpReq(
    @Schema(description = "회원 아이디", requiredMode = Schema.RequiredMode.REQUIRED)
    String userId,
    @Schema(description = "이메일", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Schema(description = "비밀번호", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) { }
