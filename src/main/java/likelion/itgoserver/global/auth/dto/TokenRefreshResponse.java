package likelion.itgoserver.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record TokenRefreshResponse(
        @Schema(description = "AccessToken", example = "thisisaccesstoken")
        String accessToken,
        @Schema(description = "RefreshToken", example = "thisisrefreshtoken")
        String refreshToken
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, refreshToken);
    }
}