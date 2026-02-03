package online.xxihye.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import online.xxihye.auth.dto.LoginReq;
import online.xxihye.auth.dto.LoginRes;
import online.xxihye.auth.dto.SignUpReq;
import online.xxihye.auth.dto.SignUpRes;
import online.xxihye.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "409", description = "중복된 userId")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpRes> signup(@RequestBody SignUpReq req) {
        return ResponseEntity.ok(authService.signUp(req));
    }

    @Operation(summary = "로그인")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
        @ApiResponse(responseCode = "403", description = "활성중인 회원 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(authService.login(req));
    }


}
