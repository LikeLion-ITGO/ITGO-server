package likelion.itgoserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.user.dto.SignUpRequest;
import likelion.itgoserver.domain.user.dto.SignUpResponse;
import likelion.itgoserver.domain.user.service.UserService;
import likelion.itgoserver.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User API", description = "User 관련 API")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "신규 사용자를 등록합니다."
    )
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("회원가입 요청 - loginId: {}", request.loginId());
        SignUpResponse response = userService.signUp(request);
        return ApiResponse.success(response, "회원가입 성공");
    }

}