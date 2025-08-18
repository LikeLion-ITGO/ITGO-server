package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "보낸 요청 리스트 아이템")
public record SentClaimItem(
        Long claimId,
        Long tradeId,
        ClaimStatus status,
        LocalDateTime regDate,
        ShareSummary share,
        Double distanceKm   // 가게간 거리(소수점 1자리)
) {
    @Schema(description = "요청을 보낸 Share 요약")
    public record ShareSummary(
            Long shareId,
            String primaryImageURL,
            String brand,
            String itemName,
            Integer quantity,
            LocalTime openTime,
            LocalTime closeTime,
            LocalDate expirationDate
    ) {
    }
}