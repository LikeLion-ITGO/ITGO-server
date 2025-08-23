package likelion.itgoserver.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.store.dto.StoreImageDraftPresignRequest;
import likelion.itgoserver.domain.store.dto.StoreImageDraftPresignResponse;
import likelion.itgoserver.domain.store.service.StoreImageService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Store-Image API", description = "가게 이미지 업로드(Presigned URL)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store-image")
public class StoreImageController {

    private final StoreImageService storeImageService;

    @Operation(summary = "가게 이미지 Presigned URL 발급")
    @PostMapping("/draft/presign")
    public ApiResponse<StoreImageDraftPresignResponse> presign(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody StoreImageDraftPresignRequest req
    ) {
        return ApiResponse.success(storeImageService.presignDraft(memberId, req));
    }

}