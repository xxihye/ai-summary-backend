package online.xxihye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginReq(
    @Schema(description = "로그인 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    String userId,

    @Schema(description = "비밀번호", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {}
