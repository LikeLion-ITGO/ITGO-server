package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;

import java.time.LocalDateTime;

@Schema(description = "받은 요청 리스트 아이템")
public record ReceivedClaimItem(
        Long claimId,
        Long wishId,
        StoreSummary store,     // wish.store 요약
        String title,           // wish.title
        String description,     // wish.description
        LocalDateTime claimAt,  // claim.regDate
        ClaimStatus status
) {
    @Schema(description = "가게 요약")
    public record StoreSummary(
            Long id,
            String name
    ) {}
}