package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import likelion.itgoserver.domain.store.entity.Address;
import likelion.itgoserver.domain.store.entity.Store;

@Schema(description = "가게 등록 요청")
public record StoreRegisterRequest(
        @Schema(description = "가게 이미지 URL", example = "https://example.com/store_image.png")
        String storeImageUrl,

        @NotBlank(message = "가게 이름은 필수입니다.")
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
    public Store toEntity() {
            Address address = address().toEntity();
        return Store.builder()
                .storeImageUrl(storeImageUrl)
                .storeName(storeName)
                .address(address)
                .operatingTime(operatingTime)
                .phoneNumber(phoneNumber)
                .description(description)
                .build();
    }
}