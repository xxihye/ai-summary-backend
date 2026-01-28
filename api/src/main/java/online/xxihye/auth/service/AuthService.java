package online.xxihye.auth.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.auth.dto.LoginReq;
import online.xxihye.auth.dto.LoginRes;
import online.xxihye.auth.dto.SignUpReq;
import online.xxihye.auth.dto.SignUpRes;
import online.xxihye.common.exception.ConflictException;
import online.xxihye.common.exception.ErrorCode;
import online.xxihye.common.exception.ForbiddenException;
import online.xxihye.common.exception.NotFoundException;
import online.xxihye.common.exception.UnauthorizedException;
import online.xxihye.security.jwt.JwtTokenProvider;
import online.xxihye.user.domain.User;
import online.xxihye.user.domain.UserStatus;
import online.xxihye.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpRes signUp(SignUpReq req) {
        if (userRepository.existsByUserId(req.userId())){
            throw new ConflictException(ErrorCode.DUPLICATE_USER_ID);
        }

        User user = new User(
            req.userId(),
            req.email(),
            passwordEncoder.encode(req.password())
        );

        User saved = userRepository.save(user);
        return new SignUpRes(saved.getUserNo(), saved.getUserId());
    }

    @Transactional(readOnly = true)
    public LoginRes login(LoginReq req) {
        User user = userRepository.findByUserId(req.userId().trim())
                                  .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS));

        //비밀번호 확인
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        //회원 상태 확인
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new ForbiddenException(ErrorCode.USER_INACTIVE);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserNo());
        return new LoginRes(accessToken);
    }
}
