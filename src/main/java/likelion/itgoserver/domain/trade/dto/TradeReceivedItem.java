package likelion.itgoserver.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "나눔받은 내역(내가 받은) 리스트 아이템")
public record TradeReceivedItem(
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

        Long giverStoreId,
        String storeImageUrl,
        String giverStoreName,
        String giverRoadAddress,
        LocalTime giverOpenTime,
        LocalTime giverCloseTime,
        String giverPhoneNumber
) {}