package likelion.itgoserver.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "나눔 상세 내역 응답")
public record TradeDetailResponse(
        Long tradeId,
        String primaryImageUrl,
        String itemName,
        String brand,
        Integer quantity,
        LocalDate expirationDate,

        StoreInfo giver,     // 나눔하는 가게
        StoreInfo receiver,  // 나눔받는 가게

        String status,           // MATCHED/COMPLETED/CANCELED
        LocalDateTime matchedAt, // trade.regDate
        LocalDateTime completedAt
) {
    @Schema(description = "가게 상세")
    public record StoreInfo(
            Long storeId,
            String storeImageUrl,
            String storeName,
            String roadAddress,
            LocalTime openTime,
            LocalTime closeTime,
            String phoneNumber
    ) {}
}
