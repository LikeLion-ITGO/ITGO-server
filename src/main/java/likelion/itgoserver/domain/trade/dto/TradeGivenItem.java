package likelion.itgoserver.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "나눔한 내역(내가 준) 리스트 아이템")
public record TradeGivenItem(
        Long tradeId,
        String status,
        String brand,
        String itemName,
        LocalTime openTime,
        LocalTime closeTime,
        LocalDate expirationDate,
        Integer wishQuantity,
        Double distanceKm,
        String receiverRoadAddress,
        LocalTime receiverOpenTime,
        LocalTime receiverCloseTime,
        String receiverPhoneNumber
) {}