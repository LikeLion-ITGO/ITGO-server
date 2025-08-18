package likelion.itgoserver.domain.trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.itgoserver.domain.trade.dto.TradeDetailResponse;
import likelion.itgoserver.domain.trade.dto.TradeGivenItem;
import likelion.itgoserver.domain.trade.dto.TradeReceivedItem;
import likelion.itgoserver.domain.trade.entity.TradeStatus;
import likelion.itgoserver.domain.trade.service.TradeListService;
import likelion.itgoserver.domain.trade.service.TradeService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trade API", description = "거래내역 조회 및 처리")
@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeListService tradeListService;

    @Operation(summary = "거래 내역 : 나눔한 내역 리스트")
    @GetMapping("/given/details")
    public ApiResponse<Page<TradeGivenItem>> listGiven(
            @CurrentMemberId Long memberId,
            @RequestParam(required = false) TradeStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(tradeListService.listGiven(memberId, status, pageable));
    }

    @Operation(summary = "거래내역 : 나눔받은 내역 리스트")
    @GetMapping("/received/details")
    public ApiResponse<Page<TradeReceivedItem>> listReceived(
            @CurrentMemberId Long memberId,
            @RequestParam(required = false) TradeStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.success(tradeListService.listReceived(memberId, status, pageable));
    }

    @Operation(summary = "나눔 상세 내역 조회")
    @GetMapping("/{tradeId}")
    public ApiResponse<TradeDetailResponse> get(@PathVariable Long tradeId) {
        return ApiResponse.success(tradeService.get(tradeId));
    }

    @Operation(summary = "나눔 완료")
    @PostMapping("/complete/{tradeId}")
    public ApiResponse<TradeDetailResponse> complete(@PathVariable Long tradeId) {
        return ApiResponse.success(tradeService.complete(tradeId));
    }

    @Operation(summary = "나눔 취소")
    @PostMapping("/cancel/{tradeId}")
    public ApiResponse<TradeDetailResponse> cancel(@PathVariable Long tradeId) {
        return ApiResponse.success(tradeService.cancel(tradeId));
    }
}