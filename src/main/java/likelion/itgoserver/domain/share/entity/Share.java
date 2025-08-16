package likelion.itgoserver.domain.share.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.support.BaseTimeEntity;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Share extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "share", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq ASC")
    @Builder.Default
    private List<ShareImage> images = new ArrayList<>();

    private String itemName;
    private String brand;
    private Integer quantity;
    private String description;
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @OneToMany(mappedBy = "share", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    public void addImage(ShareImage image) {
        image.linkShare(this);
        this.images.add(image);
    }

    public void removeImage(ShareImage image) {
        this.images.remove(image);
    }

    public boolean isAvailable() {
        return this.quantity > 0;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity < amount) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "재고가 요청 수량보다 부족합니다.");
        }
        this.quantity -= amount;
    }
}