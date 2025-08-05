package likelion.itgoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.member.entity.Role;
import likelion.itgoserver.domain.user.entity.User;

@Schema(description = "회원가입 요청")
public record SignUpRequest(
        @NotBlank(message = "로그인 ID는 필수입니다.")
        @Size(min = 4, max = 30, message = "로그인 ID는 4자 이상 30자 이하여야 합니다.")
        @Schema(description = "로그인 ID", example = "login1234")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        @Schema(description = "비밀번호", example = "password1234")
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        @Schema(description = "비밀번호", example = "password1234")
        String passwordConfirm,

        @NotBlank(message = "사용자명은 필수입니다.")
        @Size(min = 2, max = 20, message = "사용자명은 2자 이상 20자 이하여야 합니다.")
        @Schema(description = "사용자 이름", example = "김멋사")
        String username
) {
    public boolean passwordMatches() {
        return password != null && password.equals(passwordConfirm);
    }

    public Member toMember() {
        return Member.builder()
                .username(username)
                .role(Role.ROLE_MEMBER) // 기본 권한은 일반화원으로 설정
                .build();
    }

    public User toUser(String encodedPassword, Member member) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .member(member)
                .build();
    }

}