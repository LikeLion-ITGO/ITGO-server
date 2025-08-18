package likelion.itgoserver.domain.store.dto;

import java.time.LocalTime;

public interface StoreRequest {
    String storeName();
    AddressRequest address();
    LocalTime openTime();
    LocalTime closeTime();
    String phoneNumber();
    String description();
}