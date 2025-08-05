package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.store.entity.Address;
import likelion.itgoserver.domain.store.util.AddressMapper;

@Schema(description = "주소 요청")
public record AddressRequest(
        @Schema(description = "우편번호", example = "01842")
        String zipCode,

        @Schema(description = "도로명 주소", example = "서울 노원구 동일로192길 62 2층")
        String roadAddress,

        @Schema(description = "시/도", example = "서울")
        String city,

        @Schema(description = "시/군/구", example = "노원구")
        String district,

        @Schema(description = "법정동", example = "공릉동")
        String dong
) {
    public Address toEntity() {
        return AddressMapper.from(zipCode, roadAddress, city, district, dong);
    }
}