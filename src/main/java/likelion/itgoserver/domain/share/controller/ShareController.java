package likelion.itgoserver.domain.share.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.share.dto.*;
import likelion.itgoserver.domain.share.service.ShareService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share API", description = "Share 등록/조회")
@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
@Validated
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "나눔 게시글 등록")
    @PostMapping
    public ApiResponse<ShareResponse> create(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ShareUpsertRequest request) {
        ShareResponse response = shareService.create(memberId, request);
        return ApiResponse.success(response, "나눔 게시글 등록 완료.");
    }

    @Operation(summary = "Share 단건 조회")
    @GetMapping("/{shareId}")
    public ApiResponse<ShareResponse> get(@PathVariable Long shareId) {
        return ApiResponse.success(shareService.get(shareId), "나눔 게시글 단건 조회 성공");
    }

    @Operation(summary = "사용자가 올린 Share 카드 리스트")
    @GetMapping()
    public ApiResponse<Page<ShareCardResponse>> myShares(
            @CurrentMemberId Long currentMemberId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(shareService.listMyShareCards(currentMemberId, pageable), "본인의 나눔 게시글 조회 완료.");
    }

    @Operation(
            summary = "같은 동네의 Share 리스트",
            description = "내 가게와 같은 동네정보의 진행중인 Share 반환"
    )
    @GetMapping("dong")
    public ApiResponse<Page<ShareByDongResponse>> getByDong(
            @CurrentMemberId Long currentMemberId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(shareService.getByDong(currentMemberId, pageable), "같은 동네의 나눔 게시글 조회 완료.");
    }

    @Operation(summary = "나눔 게시글 수정")
    @PutMapping("/{shareId}")
    public ApiResponse<ShareResponse> update(
            @CurrentMemberId Long memberId,
            @PathVariable Long shareId,
            @Valid @RequestBody ShareUpsertRequest request
    ) {
        ShareResponse response = shareService.update(memberId, shareId, request);
        return ApiResponse.success(response, "나눔 게시글 수정 완료");
    }

    @Operation(summary = "나눔 게시글 삭제")
    @DeleteMapping("/{shareId}")
    public ApiResponse<Object> delete(
            @CurrentMemberId Long memberId,
            @PathVariable Long shareId
    ) {
        shareService.delete(memberId, shareId);
        return ApiResponse.success("나눔 게시글 삭제 완료");
    }

}