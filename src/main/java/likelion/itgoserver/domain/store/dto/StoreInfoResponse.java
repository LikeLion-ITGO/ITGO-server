package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.store.entity.Store;

@Schema(description = "가게 정보 응답")
public record StoreInfoResponse(
        @Schema(description = "가게 아이디", example = "1")
        Long storeId,
        @Schema(description = "가게 이미지 URL", example = "https://example.com/store_image.png")
        String storeImageUrl,
        @Schema(description = "가게 이름", example = "여기꼬치네")
        String storeName,

        AddressResponse address,

        @Schema(description = "가게 운영 시간", example = "10:00 ~ 19:00")
        String operatingTime,
        @Schema(description = "가게 전화번호", example = "02-1234-5678")
        String phoneNumber,
        @Schema(description = "가게 소개", example = "철길 사거리 방면 CU 옆건물 2층입니다.")
        String description
) {
    public static StoreInfoResponse of(Store store) {
        return new StoreInfoResponse(
                store.getId(),
                store.getStoreImageUrl(),
                store.getStoreName(),
                AddressResponse.from(store.getAddress()),
                store.getOperatingTime(),
                store.getPhoneNumber(),
                store.getDescription()
        );
    }
}