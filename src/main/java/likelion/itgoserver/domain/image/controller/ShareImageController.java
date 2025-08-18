package likelion.itgoserver.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.image.dto.ShareImageConfirmRequest;
import likelion.itgoserver.domain.image.dto.ShareImageListResponse;
import likelion.itgoserver.domain.image.dto.ShareImagePresignRequest;
import likelion.itgoserver.domain.image.dto.ShareImagePresignResponse;
import likelion.itgoserver.domain.image.service.ShareImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share-Image API", description = "Share 이미지 업로드((Presigned URL))")
@RestController
@RequestMapping("/api/v1/share-image")
@RequiredArgsConstructor
@Validated
public class ShareImageController {

    private final ShareImageService shareImageService;

    @Operation(
            summary = "이미지 업로드용 Presigned PUT URL 발급",
            description = """
            각 항목의 seq/ext/contentType로 업로드 키를 만들고, S3 PUT presign URL을 발급
            - objectKey 규칙: shares/{shareId}/images/{seq}_{uuid}.{ext}
            - 클라이언트는 PUT 시 presign에 서명된 Content-Type과 동일한 헤더를 전송
            """
    )
    @PostMapping("/presign/{shareId}")
    public ResponseEntity<ShareImagePresignResponse> presign(
            @PathVariable Long shareId,
            @Valid @RequestBody ShareImagePresignRequest request
    ) {
        return ResponseEntity.ok(shareImageService.presign(shareId, request));
    }

    @Operation(
            summary = "이미지 확정(커밋)",
            description = """
                    Presign으로 업로드한 이미지들을 Share에 연결
                    - 대표 이미지는 `seq=0` 규칙
                    - objectKey는 `shares/{shareId}/images/` prefix만 허용
                    """
    )
    @PostMapping("/confirm")
    public ResponseEntity<ShareImageListResponse> confirm(
            @Valid @RequestBody ShareImageConfirmRequest request
    ) {
        ShareImageListResponse response = shareImageService.confirm(request);
        return ResponseEntity.ok(response);
    }
}