package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "보낸 요청(Wish 입장) 리스트 아이템")
public record SentClaimItem(
        Long claimId,
        Long shareId,
        String primaryImageUrl, // share.images[0] (seq=0)
        String brand,           // share.brand
        String itemName,        // share.itemName
        Integer quantity,       // share.quantity
        LocalTime openTime,     // share.openTime
        LocalTime closeTime,    // share.closeTime
        LocalDate expirationDate, // share.expirationDate
        Double distanceKm,      // 가게간 거리(소수점 1자리)
        LocalDateTime claimAt,   // claim.regDate
        ClaimStatus status
) {}