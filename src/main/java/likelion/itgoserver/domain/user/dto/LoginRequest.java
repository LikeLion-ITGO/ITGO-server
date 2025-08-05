package likelion.itgoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @NotBlank(message = "로그인 ID는 필수입니다.")
        @Schema(description = "로그인 ID", example = "login1234")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Schema(description = "비밀번호", example = "password1234")
        String password
) {
}