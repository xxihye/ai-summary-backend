package online.xxihye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public record LoginRes(
    @Schema(description = """
            Access Token (JWT).
            - Authorization: Bearer <token> 헤더로 전달
            """
    )
    String accessToken
) {
}
