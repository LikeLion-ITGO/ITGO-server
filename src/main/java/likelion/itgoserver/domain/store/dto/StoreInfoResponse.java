package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.store.entity.Store;

import java.time.LocalTime;

@Schema(description = "가게 정보 응답")
public record StoreInfoResponse(
        @Schema(description = "가게 아이디", example = "1")
        Long storeId,
        @Schema(description = "가게 이미지 URL", example = "https://example.com/store_image.png")
        String storeImageUrl,
        @Schema(description = "가게 이름", example = "여기꼬치네")
        String storeName,

        AddressResponse address,

        @Schema(description = "영업 시작 시간", example = "09:00")
        LocalTime openTime,
        @Schema(description = "영업 종료 시간", example = "18:00")
        LocalTime closeTime,

        @Schema(description = "가게 전화번호", example = "02-1234-5678")
        String phoneNumber,
        @Schema(description = "가게 소개", example = "철길 사거리 방면 CU 옆건물 2층입니다.")
        String description,

        @Schema(description = "나눔을 준 횟수", example = "11")
        Integer giveTimes,

        @Schema(description = "나눔을 받은 횟수", example = "7")
        Integer receivedTimes
) {
    public static StoreInfoResponse of(Store store, String imageUrl) {
        return new StoreInfoResponse(
                store.getId(),
                imageUrl,
                store.getStoreName(),
                AddressResponse.from(store.getAddress()),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getPhoneNumber(),
                store.getDescription(),
                store.getGiveTimes(),
                store.getReceivedTimes()
        );
    }
}