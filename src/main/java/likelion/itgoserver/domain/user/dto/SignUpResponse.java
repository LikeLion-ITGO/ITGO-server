package likelion.itgoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.member.entity.Role;

public record SignUpResponse(
        @Schema(description = "User 아이디", example = "1")
        Long userId,
        @Schema(description = "Member 아이디", example = "1")
        Long memberId,
        @Schema(description = "로그인 ID", example = "login1234")
        String loginId,
        @Schema(description = "사용자 이름", example = "김멋사")
        String username,
        @Schema(description = "회원권한", example = "ROLE_MEMBER")
        Role role,
        String accessToken,
        String refreshToken
) {
    public static SignUpResponse of(Long userId, Long memberId, String loginId, String username,
                                    Role role, String accessToken, String refreshToken) {
        return new SignUpResponse(userId, memberId, loginId, username, role, accessToken, refreshToken);
    }
}
