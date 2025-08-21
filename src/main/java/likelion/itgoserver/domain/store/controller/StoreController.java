package likelion.itgoserver.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.store.dto.StoreInfoResponse;
import likelion.itgoserver.domain.store.dto.StoreRegisterRequest;
import likelion.itgoserver.domain.store.dto.StoreUpdateRequest;
import likelion.itgoserver.domain.store.service.StoreService;
import likelion.itgoserver.global.response.ApiResponse;
import likelion.itgoserver.global.support.resolver.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store")
@Tag(name = "Store API", description = "가게 관련 API")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "가게 등록",
            description = "현재 로그인된 회원이 가게 정보를 등록\n\n" +
                    "`Authorization: Bearer {accessToken}`"
    )
    @PostMapping
    public ApiResponse<StoreInfoResponse> registerStore(
            @CurrentMemberId Long memberId,
            @RequestBody @Valid StoreRegisterRequest request
    ) {
        StoreInfoResponse response = storeService.registerStore(memberId, request);
        return ApiResponse.success(response, "가게 등록 성공");
    }

    @Operation(
            summary = "가게 정보 조회",
            description = "storeId 값으로 가게 정보를 조회"
    )
    @GetMapping("/{storeId}")
    public ApiResponse<StoreInfoResponse> getStore(
            @PathVariable Long storeId
    ) {
        StoreInfoResponse response = storeService.getStore(storeId);
        return ApiResponse.success(response, "가게 조회 성공");
    }

    @Operation(
            summary = "내 가게 조회",
            description = "현재 로그인된 회원의 가게 정보를 조회\n\n" +
                    "`Authorization: Bearer {accessToken}`"
    )
    @GetMapping("/me")
    public ApiResponse<StoreInfoResponse> getMyStore(
            @CurrentMemberId Long memberId
    ) {
        StoreInfoResponse response = storeService.getStoreByMemberId(memberId);
        return ApiResponse.success(response, "가게 조회 성공");
    }



    @Operation(
            summary = "가게 정보 수정",
            description = "현재 로그인된 회원의 가게 정보를 수정\n\n" +
                    "`Authorization: Bearer {accessToken}`"
    )
    @PutMapping
    public ApiResponse<StoreInfoResponse> updateStore(
            @CurrentMemberId Long memberId,
            @RequestBody @Valid StoreUpdateRequest request
    ) {
        StoreInfoResponse response = storeService.updateStore(memberId, request);
        return ApiResponse.success(response, "가게 수정 성공");
    }

    @Operation(
            summary = "가게 삭제",
            description = "현재 로그인된 회원의 가게 정보를 삭제\n\n" +
                    "`Authorization: Bearer {accessToken}`"
    )
    @DeleteMapping
    public ApiResponse<Void> deleteStore(
            @CurrentMemberId Long memberId
    ) {
        storeService.deleteStore(memberId);
        return ApiResponse.success(null, "가게 삭제 성공");
    }
}