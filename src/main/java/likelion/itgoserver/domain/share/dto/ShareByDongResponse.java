package likelion.itgoserver.domain.share.dto;

import likelion.itgoserver.domain.share.entity.StorageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ShareByDongResponse(
        Long shareId,
        String itemName,
        String brand,
        Integer quantity,
        LocalDate expirationDate,
        StorageType storageType,
        LocalTime openTime,
        LocalTime closeTime,
        String primaryImageUrl,
        Double distanceKm,
        LocalDateTime regDate
) {
}
