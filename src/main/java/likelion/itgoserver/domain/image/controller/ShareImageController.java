package likelion.itgoserver.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.image.dto.*;
import likelion.itgoserver.domain.image.service.ShareImageService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share-Image API", description = "Share 이미지 업로드((Presigned URL))")
@RestController
@RequestMapping("/api/v1/share-image")
@RequiredArgsConstructor
@Validated
public class ShareImageController {

    private final ShareImageService shareImageService;

    @Operation(summary = "이미지 업로드용 Presigned PUT URL 발급")
    @PostMapping("/draft/presign")
    public ApiResponse<ShareImageDraftPresignResponse> presignDraft(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ShareImageDraftPresignRequest request
    ) {
        return ApiResponse.success(shareImageService.presignDraft(memberId, request));
    }

}