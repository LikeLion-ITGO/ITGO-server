package likelion.itgoserver.global.auth.service;

import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.user.dto.LoginRequest;
import likelion.itgoserver.domain.user.dto.LoginResponse;
import likelion.itgoserver.domain.user.entity.User;
import likelion.itgoserver.domain.user.repository.UserRepository;
import likelion.itgoserver.global.auth.dto.TokenRefreshResponse;
import likelion.itgoserver.global.auth.jwt.JwtProvider;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "존재하지 않는 로그인 ID입니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        Long userId = user.getId();
        Member member = user.getMember();

        // JWT 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // RefreshToken Redis 저장 or 갱신
        refreshTokenService.save(userId, refreshToken);

        return LoginResponse.of(
                user.getId(),
                user.getMember().getId(),
                user.getMember().getUsername(),
                user.getMember().getRole(),
                accessToken,
                refreshToken
        );
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        Long userId = jwtProvider.getUserId(accessToken);
        refreshTokenService.delete(userId);
    }

    /**
     * 토큰 재발급
     */
    public TokenRefreshResponse reissueTokens(String refreshToken) {

        // 1. 리프레시 토큰 JWT 유효성 검증
        if (!jwtProvider.isValidRefreshToken(refreshToken)) {
            throw new CustomException(GlobalErrorCode.INVALID_REFRESH_TOKEN, "유효하지 않은 Refresh 토큰입니다.");
        }

        // 2. 사용자 ID 추출
        Long userId = jwtProvider.getUserId(refreshToken);

        // 3. Redis에 저장된 토큰과 일치 여부 + 블랙리스트 검증 포함
        if (!refreshTokenService.validateToken(userId, refreshToken)) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED, "유효하지 않거나 일치하지 않는 Refresh 토큰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "사용자 없음"));
        Member member = user.getMember();

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), member.getId(), member.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        refreshTokenService.save(userId, newRefreshToken);
        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }

}