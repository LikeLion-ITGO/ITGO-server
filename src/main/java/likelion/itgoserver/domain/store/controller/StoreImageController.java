package likelion.itgoserver.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.itgoserver.domain.store.dto.StoreImageDtos.*;
import likelion.itgoserver.domain.store.service.StoreImageService;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import likelion.itgoserver.global.infra.s3.service.S3ImageService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Duration;

@Tag(name = "Store-Image API", description = "가게 이미지 업로드(Presigned URL)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store-image")
public class StoreImageController {

    private final S3ImageService s3ImageService;
    private final PublicUrlResolver publicUrlResolver;
    private final StoreImageService storeImageService;

    @Operation(summary = "가게 이미지 Presigned URL 발급")
    @PostMapping("/presign/{storeId}")
    public ApiResponse<PresignResponse> presign(
            @CurrentMemberId Long memberId,
            @PathVariable Long storeId,
            @RequestBody StoreImagePresignRequest req
    ) {
        // 소유자 검증
        storeImageService.validateStoreOwner(memberId, storeId);

        // Key 생성 + Presigned PUT URL 발급
        String key = s3ImageService.storeKey(storeId, req.ext());
        URL putUrl = s3ImageService.createPresignedPutUrl(key, req.contentType(), Duration.ofMinutes(10));

        // 미리보기 URL
        String preview = publicUrlResolver.toUrl(key);
        return ApiResponse.success(
                new PresignResponse(putUrl.toString(), key, preview),
                "Presigned URL 발급 완료"
        );
    }

    @Operation(summary = "가게 이미지 업로드 확정: DB 반영")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(
            @CurrentMemberId Long memberId,
            @RequestBody StoreImageConfirmRequest req
    ) {
        storeImageService.confirmStoreImage(memberId, req);
        return ApiResponse.success(null, "가게 이미지 등록 완료");
    }
}
