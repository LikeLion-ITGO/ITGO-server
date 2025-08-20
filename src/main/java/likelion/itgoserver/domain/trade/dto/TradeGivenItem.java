package likelion.itgoserver.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "나눔한 내역(내가 준) 리스트 아이템")
public record TradeGivenItem(
        Long tradeId,
        String status,
        LocalDateTime completedAt,
        LocalDateTime canceledAt,
        LocalDateTime AcceptedAt,

        String itemImageUrl,
        String brand,
        String itemName,
        LocalTime openTime,
        LocalTime closeTime,
        LocalDate expirationDate,
        Integer wishQuantity,
        Double distanceKm,

        Long receiverStoreId,
        String storeImageUrl,
        String receiverStoreName,
        String receiverRoadAddress,
        LocalTime receiverOpenTime,
        LocalTime receiverCloseTime,
        String receiverPhoneNumber
) {}