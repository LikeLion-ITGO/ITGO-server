package likelion.itgoserver.domain.claim.dto;

import java.time.LocalTime;

// 빠른 신청용 요청 DTO
public record QuickClaimRequest(
        Long shareId,
        Integer quantity,
        LocalTime openTime,
        LocalTime closeTime,
        String title,
        String description
) {}