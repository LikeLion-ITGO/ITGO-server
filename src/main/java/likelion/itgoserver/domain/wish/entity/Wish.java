package likelion.itgoserver.domain.wish.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.global.support.BaseTimeEntity;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_wish_store_active", columnList = "store_id,isActive"),
        @Index(name = "idx_wish_regdate", columnList = "regDate")
})
public class Wish extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String title; // 제목

    @Column(nullable = false, length = 100)
    private String itemName; // 품목명

    @Column(length = 100)
    private String brand; // 브랜드 (선택)

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(length = 500)
    private String description; // 설명 (선택)

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "wish", fetch = FetchType.LAZY)
    @OrderBy("regDate DESC")
    private List<Claim> claims = new ArrayList<>();

    public void close() {
        this.isActive = false;
    }
}