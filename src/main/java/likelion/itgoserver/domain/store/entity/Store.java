package likelion.itgoserver.domain.store.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.store.dto.StoreUpdateRequest;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String storeImageUrl; // 가게 이미지 URL

    @Column(nullable = false, length = 50)
    private String storeName; // 가게 이름

    @Column(nullable = false, length = 100)
    private Address address; // 가게 주소

    @Column(nullable = false, length = 20)
    private String phoneNumber; // 가게 전화번호

    @Column(nullable = false, length = 50)
    private String operatingTime; // 가게 운영 시간

    @Column(length = 500)
    private String description; // 가게 소개

    public void update(StoreUpdateRequest request) {
        this.storeImageUrl = request.storeImageUrl();
        this.storeName = request.storeName();
        this.address = request.address().toEntity();
        this.operatingTime = request.operatingTime();
        this.phoneNumber = request.phoneNumber();
        this.description = request.description();
    }

}