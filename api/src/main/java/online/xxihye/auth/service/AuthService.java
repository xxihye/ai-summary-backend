package online.xxihye.auth.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.auth.dto.LoginReq;
import online.xxihye.auth.dto.LoginRes;
import online.xxihye.auth.dto.SignUpReq;
import online.xxihye.auth.dto.SignUpRes;
import online.xxihye.user.domain.User;
import online.xxihye.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpRes signUp(SignUpReq req) {
        if (userRepository.existsByUserId(req.userId())){
            throw new IllegalArgumentException("email already exists");
        }

        User user = new User(
            req.userId(),
            req.email(),
            passwordEncoder.encode(req.password())
        );

        User saved = userRepository.save(user);
        return new SignUpRes(saved.getUserNo(), saved.getUserId());
    }

    public LoginRes login(LoginReq req) {
        User user = userRepository.findByUserId(req.userId())
                                  .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        return new LoginRes(user.getUserNo(), user.getUserId());
    }
}
