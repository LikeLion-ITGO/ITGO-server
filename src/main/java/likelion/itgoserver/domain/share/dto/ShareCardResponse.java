package likelion.itgoserver.domain.share.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "사용자가 등록한 Share 카드")
public record ShareCardResponse(
        Long shareId,
        String itemName,
        String brand,
        Integer quantity,
        LocalDate expirationDate,
        Boolean freshCertified,
        LocalTime openTime,
        LocalTime closeTime,
        LocalDateTime regDate,
        String primaryImageUrl,
        Integer claimTotalCount
) {
}
