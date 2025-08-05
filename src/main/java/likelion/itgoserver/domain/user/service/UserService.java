package likelion.itgoserver.domain.user.service;

import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.member.repository.MemberRepository;
import likelion.itgoserver.domain.user.dto.SignUpRequest;
import likelion.itgoserver.domain.user.dto.SignUpResponse;
import likelion.itgoserver.domain.user.entity.User;
import likelion.itgoserver.domain.user.repository.UserRepository;
import likelion.itgoserver.global.auth.jwt.JwtProvider;
import likelion.itgoserver.global.auth.service.RefreshTokenService;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 중복 검증
        validateDuplicateLoginId(request.loginId());
        validateDuplicateUsername(request.username());

        // 2. 비밀번호 확인
        validatePasswordMatch(request);

        // 3. Member 생성
        Member member = request.toMember();

        // 4. User 생성 + Member 관계 매핑
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = request.toUser(encodedPassword, member);

        // 5. 저장
        Member savedMember = memberRepository.save(member);
        User savedUser = userRepository.save(user);

        // 6. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(savedUser.getId(), savedUser.getMember().getId(), savedMember.getRole());
        String refreshToken = jwtProvider.createRefreshToken(savedUser.getId());

        // 7. 리프레시 토큰 저장
        refreshTokenService.save(savedUser.getId(), refreshToken);

        return SignUpResponse.of(
                savedUser.getId(),
                savedMember.getId(),
                savedUser.getLoginId(),
                savedMember.getUsername(),
                savedMember.getRole(),
                accessToken,
                refreshToken
        );
    }

    /**
     * 비밀번호 확인 로직
     */
    private void validatePasswordMatch(SignUpRequest request) {
        if (!request.passwordMatches()) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 로그인 ID 중복 체크
     */
    private void validateDuplicateLoginId(String loginId) {
        if (isLoginIdDuplicated(loginId)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미 존재하는 로그인 아이디입니다.");
        }
    }

    /**
     * 사용자명 중복 체크
     */
    private void validateDuplicateUsername(String username) {
        if (isUsernameDuplicated(username)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미 존재하는 회원명입니다.");
        }
    }

    public boolean isLoginIdDuplicated(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public boolean isUsernameDuplicated(String username) {
        return memberRepository.existsByUsername(username);
    }

}