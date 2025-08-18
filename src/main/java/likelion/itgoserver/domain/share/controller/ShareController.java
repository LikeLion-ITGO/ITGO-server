package likelion.itgoserver.domain.share.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.share.dto.ShareCardResponse;
import likelion.itgoserver.domain.share.dto.ShareResponse;
import likelion.itgoserver.domain.share.dto.ShareUpsertRequest;
import likelion.itgoserver.domain.share.service.ShareService;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<ShareResponse> create(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ShareUpsertRequest request) {
        ShareResponse response = shareService.create(memberId, request);
        // Location 헤더 설정 (REST 관례)
        URI location = URI.create("/api/shares/" + response.shareId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Share 단건 조회")
    @GetMapping("/{shareId}")
    public ResponseEntity<ShareResponse> get(@PathVariable Long shareId) {
        return ResponseEntity.ok(shareService.get(shareId));
    }

    @Operation(summary = "사용자가 올린 Share 카드 리스트")
    @GetMapping()
    public ResponseEntity<Page<ShareCardResponse>> myShares(
            @CurrentMemberId Long currentMemberId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(shareService.listMyShareCards(currentMemberId, pageable));
    }

}