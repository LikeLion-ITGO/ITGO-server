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
                // 무한스크롤 : 받은 요청
                @Index(name = "idx_claim_share_regdate_id", columnList = "share_id, reg_date, id"),
                // 무한스크롤 : 보낸 요청
                @Index(name = "idx_claim_wish_regdate_id",  columnList = "wish_id,  reg_date, id"),
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
        // 이미 최종 상태면 멱등 처리
        if (status == ClaimStatus.CANCELED || status == ClaimStatus.REJECTED) return;

        // PENDING 또는 ACCEPTED → CANCELED 전환 허용
        if (status == ClaimStatus.PENDING || status == ClaimStatus.ACCEPTED) {
            status = ClaimStatus.CANCELED;
            decidedAt = LocalDateTime.now();
        }
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