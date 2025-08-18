package likelion.itgoserver.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import likelion.itgoserver.domain.store.entity.Address;
import likelion.itgoserver.domain.store.entity.Store;

import java.time.LocalTime;

@Schema(description = "가게 등록 요청")
public record StoreRegisterRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Schema(description = "가게 이름", example = "여기꼬치네")
        String storeName,

        AddressRequest address,

        @Schema(description = "영업 시작 시간", example = "09:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime openTime,
        @Schema(description = "영업 종료 시간", example = "18:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime closeTime,

        @Schema(description = "가게 전화번호", example = "02-1234-5678")
        String phoneNumber,

        @Schema(description = "가게 소개", example = "철길 사거리 방면 CU 옆건물 2층입니다.")
        String description
) implements StoreRequest {
    public Store toEntity() {
            Address address = address().toEmbeddable();
        return Store.builder()
                .storeName(storeName)
                .address(address)
                .openTime(openTime)
                .closeTime(closeTime)
                .phoneNumber(phoneNumber)
                .description(description)
                .build();
    }
}