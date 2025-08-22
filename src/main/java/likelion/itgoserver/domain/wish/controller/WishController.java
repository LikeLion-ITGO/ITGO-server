package likelion.itgoserver.domain.wish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.wish.dto.WishCardResponse;
import likelion.itgoserver.domain.wish.dto.WishCreateAndMatchResponse;
import likelion.itgoserver.domain.wish.dto.WishUpsertRequest;
import likelion.itgoserver.domain.wish.service.WishService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wish API", description = "필요한 재고 요청 생성 및 즉시 매칭")
@RestController
@RequestMapping("/api/v1/wish")
@RequiredArgsConstructor
@Validated
public class WishController {

    private final WishService wishService;

    @Operation(
            summary = "Wish 생성 + 즉시 매칭",
            description = """
            사용자가 Wish를 저장하면, 같은 트랜잭션 흐름에서 조건을 만족하는 Share 리스트를 같이 반환
            기본 매칭 조건:
            1) wish.store.address.dong == share.store.address.dong
            2) wish.itemName == share.itemName
            3) share.quantity >= wish.quantity
            4) 거래 가능 시간대가 겹침 (open/close overlap)
            + 유통기한 및 km 반경 선택 옵션
            """
    )
    @PostMapping("/match")
    public ApiResponse<WishCreateAndMatchResponse> createAndMatch(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody WishUpsertRequest request,
            @RequestParam(defaultValue = "3") double radiusKm,     // 선택: 근처 반경(없으면 동 기준만)
            @RequestParam(defaultValue = "10") int size            // 한 번에 보여줄 개수
    ) {
        var resp = wishService.createAndMatch(memberId, request, radiusKm, PageRequest.of(0, size));
        return ApiResponse.success(resp, "나눔 요청 등록 완료");
    }

    @Operation(summary = "사용자가 올린 모든 Wish 카드 리스트")
    @GetMapping()
    public ApiResponse<Page<WishCardResponse>> myShares(
            @CurrentMemberId Long memberId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(wishService.listMyWishCards(memberId, pageable), "사용자가 등록한 모든 요청 조회 완료");
    }

    @Operation(summary = "사용자가 올린 진행중인 Wish 카드 리스트")
    @GetMapping("/active")
    public ApiResponse<Page<WishCardResponse>> myActiveShares(
            @CurrentMemberId Long memberId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(wishService.listMyActiveWishCards(memberId, pageable), "사용자가 등록한 진행중인 요청 조회 완료");
    }

}