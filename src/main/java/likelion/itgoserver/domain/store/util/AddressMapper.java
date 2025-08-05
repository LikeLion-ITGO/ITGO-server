package likelion.itgoserver.domain.store.util;

import likelion.itgoserver.domain.store.entity.Address;

public class AddressMapper {

    public static Address from(String zipCode, String roadAddress, String city, String district, String dong) {
        String detail = extractDetail(roadAddress, city, district);
        return Address.builder()
                .zipCode(zipCode)
                .city(city)
                .district(district)
                .detail(detail)
                .dong(dong)
                .build();
    }

    private static String extractDetail(String roadAddress, String city, String district) {
        String prefix = city + " " + district + " ";
        return roadAddress.replace(prefix, "");
    }
}