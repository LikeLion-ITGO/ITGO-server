package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.store.entity.Address;

@Schema(description = "주소 응답")
public record AddressResponse(
        @Schema(description = "도로명 주소", example = "서울 노원구 동일로192길 62 2층")
        String roadAddress,

        @Schema(description = "법정동", example = "공릉동")
        String dong,

        @Schema(description = "위도", example = "37.6267705")
        Double latitude,

        @Schema(description = "경도", example = "127.0763917")
        Double longitude
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getRoadAddress(),
                address.getDong(),
                address.getLatitude(),
                address.getLongitude()
        );
    }

}