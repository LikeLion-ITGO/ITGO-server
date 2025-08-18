package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;

import java.time.LocalDateTime;

@Schema(description = "받은 요청 리스트 아이템")
public record ReceivedClaimItem(
        Long claimId,
        Long tradeId,
        ClaimStatus status,
        LocalDateTime regDate,
        WishSummary wish,
        StoreSummary store
) {
    @Schema(description = "요청자가 보낸 Wish 요약")
    public record WishSummary(
            Long wishId,
            String title,
            String description
    ) {}

    @Schema(description = "요청자 가게 요약")
    public record StoreSummary(
            Long id,
            String name
    ) {}
}