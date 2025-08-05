package likelion.itgoserver.global.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.user.dto.LoginRequest;
import likelion.itgoserver.domain.user.dto.LoginResponse;
import likelion.itgoserver.global.auth.dto.TokenRefreshResponse;
import likelion.itgoserver.global.auth.service.AuthService;
import likelion.itgoserver.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "로그인 후 Token 발급"
    )
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // JWT를 응답 헤더에 추가
        response.setHeader("Authorization", "Bearer " + loginResponse.accessToken());
        response.setHeader("X-Refresh-Token", loginResponse.refreshToken());

        return ApiResponse.success(loginResponse, "로그인 성공");
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "Redis에서 RefreshToken 제거\n\n" +
                    "`Authorization: Bearer {accessToken}`"
    )
    public ApiResponse<Object> logout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization")
            String accessToken
    ) {
        log.info("로그아웃 요청 - accessToken : {}", accessToken);
        authService.logout(accessToken);
        return ApiResponse.success("로그아웃 성공");
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    @Operation(
            summary = "토큰 재발급",
            description = "RefreshToken 기반으로 AccessToken 및 RefreshToken 재발급\n\n" +
                    "`X-Refresh-Token: {refreshToken}`"
    )
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @RequestHeader("X-Refresh-Token")
            String refreshToken,
            HttpServletResponse response
    ) {
        TokenRefreshResponse newTokens = authService.reissueTokens(refreshToken);

        // 헤더에 추가
        response.setHeader("Authorization", "Bearer " + newTokens.accessToken());
        response.setHeader("X-Refresh-Token", newTokens.refreshToken());

        return ApiResponse.success(newTokens, "Access + Refresh Token 재발급 성공");
    }

}