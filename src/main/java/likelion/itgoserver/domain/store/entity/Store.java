package likelion.itgoserver.domain.store.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.store.dto.StoreUpdateRequest;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "store", fetch = FetchType.LAZY)
    private Member owner;

    @Column(length = 255)
    private String storeImageKey; // S3 Key

    @Column(nullable = false, length = 50)
    private String storeName; // 가게 이름

    @Embedded
    private Address address; // 가게 주소

    @Column(nullable = false, length = 20)
    private String phoneNumber; // 가게 전화번호

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Builder.Default
    @Column(nullable = false)
    private Integer giveTimes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer receivedTimes = 0;

    @Column(length = 500)
    private String description; // 가게 소개

    public void update(StoreUpdateRequest request) {
        this.storeName = request.storeName();
        this.address = request.address().toEmbeddable();
        this.openTime = request.openTime();
        this.closeTime = request.closeTime();
        this.phoneNumber = request.phoneNumber();
        this.description = request.description();
    }

    /**
     * 연관관계 메서드
     */
    public void assignOwner(Member member) {
        this.owner = member;
    }

    public void updateImageKey(String imageKey) {
        this.storeImageKey = imageKey;
    }

    public void increaseGiveTimes() { this.giveTimes += 1; }
    public void increaseReceivedTimes() { this.receivedTimes += 1; }
}