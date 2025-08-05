package likelion.itgoserver.domain.store.dto;

public interface StoreRequest {
    String storeImageUrl();
    String storeName();
    AddressRequest address();
    String operatingTime();
    String phoneNumber();
    String description();
}