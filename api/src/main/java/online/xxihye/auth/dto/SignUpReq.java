package online.xxihye.auth.dto;

public record SignUpReq(
    String userId,
    String email,
    String password
) { }
