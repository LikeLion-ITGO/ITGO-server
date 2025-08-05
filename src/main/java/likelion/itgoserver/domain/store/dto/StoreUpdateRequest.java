package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 수정 요청")
public record StoreUpdateRequest(
        @Schema(description = "가게 이미지 URL", example = "https://example.com/store_image.png")
        String storeImageUrl,

        @Schema(description = "가게 이름", example = "여기꼬치네")
        String storeName,

        AddressRequest address,

        @Schema(description = "가게 운영 시간", example = "10:00 ~ 19:00")
        String operatingTime,

        @Schema(description = "가게 전화번호", example = "02-1234-5678")
        String phoneNumber,

        @Schema(description = "가게 소개", example = "철길 사거리 방면 CU 옆건물 2층입니다.")
        String description
) implements StoreRequest {
}