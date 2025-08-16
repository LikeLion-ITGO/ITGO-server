package likelion.itgoserver.domain.claim.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.wish.entity.Wish;
import likelion.itgoserver.global.support.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "claim",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wish_share", columnNames = {"wish_id","share_id"})
        },
        indexes = {
                @Index(name = "idx_claim_wish", columnList = "wish_id"),
                @Index(name = "idx_claim_share", columnList = "share_id"),
                @Index(name = "idx_claim_status_regdate", columnList = "status,regDate")
        }
)
public class Claim extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="wish_id", nullable=false)
    private Wish wish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_id", nullable = false)
    private Share share;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_store_id", nullable = false)
    private Store requesterStore;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    private LocalDateTime decidedAt;

    // TODO : 매칭 후 새로운 인스턴스 생성할지 말지...?

    // TODO : 매칭 후 거래 완료 처리..?

    public void accept() {
        if (status != ClaimStatus.PENDING) return;
        status = ClaimStatus.ACCEPTED;
        decidedAt = LocalDateTime.now();
    }

    public void reject() {
        if (status != ClaimStatus.PENDING) return;
        status = ClaimStatus.REJECTED;
        decidedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != ClaimStatus.PENDING) return;
        status = ClaimStatus.CANCELED;
        decidedAt = LocalDateTime.now();
    }

    public static Claim from(Wish wish, Share share) {
        return Claim.builder()
                .wish(wish)
                .share(share)
                .requesterStore(wish.getStore())
                .quantity(wish.getQuantity())
                .status(ClaimStatus.PENDING)
                .build();
    }
}