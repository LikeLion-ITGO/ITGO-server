package likelion.itgoserver.domain.trade.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.wish.entity.Wish;
import likelion.itgoserver.global.support.BaseTimeEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "trade",
        uniqueConstraints = @UniqueConstraint(name = "uk_trade_claim", columnNames = "claim_id"),
        indexes = {
                @Index(name = "idx_trade_giver_store", columnList = "giver_store_id"),
                @Index(name = "idx_trade_receiver_store", columnList = "receiver_store_id"),
                @Index(name = "idx_trade_status_regdate", columnList = "status,regDate")
        }
)
public class Trade extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_id", nullable = false)
    private Share share;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wish_id", nullable = false)
    private Wish wish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_store_id", nullable = false)
    private Store giverStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_store_id", nullable = false)
    private Store receiverStore;

    @Column(length = 512)
    private String primaryImageKey;

    private String itemName;
    private String brand;
    private Integer quantity;
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeStatus status;

    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;

    public static Trade fromAcceptedClaim(Claim claim, String primaryImageKeySnapshot) {
        var s = claim.getShare();
        var w = claim.getWish();
        return Trade.builder()
                .claim(claim)
                .share(s)
                .wish(w)
                .giverStore(s.getStore())
                .receiverStore(w.getStore())
                .primaryImageKey(primaryImageKeySnapshot)
                .itemName(s.getItemName())
                .brand(s.getBrand())
                .quantity(claim.getQuantity())
                .expirationDate(s.getExpirationDate())
                .status(TradeStatus.MATCHED)
                .build();
    }

    public void complete() {
        if (this.status == TradeStatus.MATCHED) {
            this.status = TradeStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (this.status == TradeStatus.MATCHED) {
            this.status = TradeStatus.CANCELED;
            this.canceledAt = LocalDateTime.now();
        }
    }
}