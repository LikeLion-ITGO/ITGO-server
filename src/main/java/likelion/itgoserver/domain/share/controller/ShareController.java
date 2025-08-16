package likelion.itgoserver.domain.share.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.share.dto.ShareResponse;
import likelion.itgoserver.domain.share.dto.ShareUpsertRequest;
import likelion.itgoserver.domain.share.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Share API", description = "Share 등록/조회")
@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
@Validated
public class ShareController {

    private final ShareService shareService;

    @Operation(
            summary = "나눔 등록",
            description = """
            나눔 재고 관련 메타데이터 생성 (이미지 업로드는 별도: presign → S3 PUT → confirm)
            """
    )
    @PostMapping
    public ResponseEntity<ShareResponse> create(@Valid @RequestBody ShareUpsertRequest request) {
        ShareResponse response = shareService.create(request);
        // Location 헤더 설정 (REST 관례)
        URI location = URI.create("/api/shares/" + response.shareId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Share 단건 조회")
    @GetMapping("/{shareId}")
    public ResponseEntity<ShareResponse> get(@PathVariable Long shareId) {
        return ResponseEntity.ok(shareService.get(shareId));
    }
}