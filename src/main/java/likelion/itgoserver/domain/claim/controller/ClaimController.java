package likelion.itgoserver.domain.claim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.claim.dto.*;
import likelion.itgoserver.domain.claim.service.ClaimService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Claim API", description = "거래 신청(Claim) 생성/처리/조회")
@RestController
@RequestMapping("/api/v1/claim")
@RequiredArgsConstructor
@Validated
public class ClaimController {

    private final ClaimService claimService;

    @Operation(summary = "거래 신청 생성")
    @PostMapping()
    public ApiResponse<ClaimResponse> create(@Valid @RequestBody ClaimCreateRequest request) {
        var resp = claimService.request(request.wishId(), request.shareId());
        return ApiResponse.success(resp);
    }

    @Operation(summary = "빠른 신청(동네 재고에서 바로 신청)")
    @PostMapping("/quick")
    public ApiResponse<ClaimResponse> quick(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody QuickClaimRequest request
    ) {
        return ApiResponse.success(claimService.requestQuick(memberId, request));
    }

    @Operation(summary = "거래 신청 취소")
    @PostMapping("/cancel/{claimId}")
    public ApiResponse<ClaimResponse> cancel(@PathVariable Long claimId) {
        return ApiResponse.success(claimService.cancel(claimId));
    }

    @Operation(summary = "거래 신청 수락")
    @PostMapping("/accept/{claimId}")
    public ApiResponse<ClaimResponse> accept(@PathVariable Long claimId) {
        return ApiResponse.success(claimService.accept(claimId));
    }

    @Operation(summary = "거래 신청 거절")
    @PostMapping("/reject/{claimId}")
    public ApiResponse<ClaimResponse> reject(@PathVariable Long claimId) {
        return ApiResponse.success(claimService.reject(claimId));
    }

    @Operation(summary = "특정 Wish의 보낸 요청 목록")
    @GetMapping("/sent/{wishId}")
    public ApiResponse<Slice<SentClaimItem>> listByWish(
            @PathVariable Long wishId,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(claimService.sentByWish(wishId, pageable));
    }

    @Operation(summary = "특정 Share의 받은 요청 목록")
    @GetMapping("/received/{shareId}")
    public ApiResponse<Slice<ReceivedClaimItem>> listByShare(
            @PathVariable Long shareId,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(claimService.receivedByShare(shareId, pageable));
    }

}