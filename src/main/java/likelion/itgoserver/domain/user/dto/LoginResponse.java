package likelion.itgoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.member.entity.Role;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "User 아이디", example = "1")
        Long userId,
        @Schema(description = "Member 아이디", example = "1")
        Long memberId,
        @Schema(description = "사용자 이름", example = "김멋사")
        String username,
        @Schema(description = "회원권한", example = "ROLE_MEMBER")
        Role role,
        @Schema(description = "AccessToken", example = "thisisaccesstoken")
        String accessToken,
        @Schema(description = "RefreshToken", example = "thisisrefreshtoken")
        String refreshToken
) {
    public static LoginResponse of(Long userId, Long memberId, String username,
                                   Role role, String accessToken, String refreshToken) {
        return new LoginResponse(userId, memberId, username, role, accessToken, refreshToken);
    }
}