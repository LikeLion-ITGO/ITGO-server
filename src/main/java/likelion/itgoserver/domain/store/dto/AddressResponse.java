package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.store.entity.Address;

@Schema(description = "주소 응답")
public record AddressResponse(
        @Schema(description = "우편번호", example = "01842")
        String zipCode,

        @Schema(description = "시/도", example = "서울")
        String city,

        @Schema(description = "시/군/구", example = "노원구")
        String district,

        @Schema(description = "상세 주소", example = "동일로192길 62 2층")
        String detail,

        @Schema(description = "행정동", example = "공릉동")
        String dong
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getZipCode(),
                address.getCity(),
                address.getDistrict(),
                address.getDetail(),
                address.getDong()
        );
    }
}