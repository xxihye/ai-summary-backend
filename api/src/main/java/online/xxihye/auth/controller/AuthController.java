package online.xxihye.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import online.xxihye.auth.dto.LoginReq;
import online.xxihye.auth.dto.LoginRes;
import online.xxihye.auth.dto.SignUpReq;
import online.xxihye.auth.dto.SignUpRes;
import online.xxihye.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SignUpRes> signup(@RequestBody SignUpReq req) {
        return ResponseEntity.ok(authService.signUp(req));
    }

    @Operation(description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(authService.login(req));
    }


}
