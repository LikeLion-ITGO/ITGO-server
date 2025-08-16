package likelion.itgoserver.domain.wish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.share.entity.StorageType;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "매칭된 Share 카드 정보")
public record WishMatchItem(
        Long shareId,
        String itemName,
        String brand,
        Integer quantity,
        LocalDate expirationDate,
        StorageType storageType,
        LocalTime openTime,
        LocalTime closeTime,
        String primaryImageUrl,    // seq=0
        Long minutesAgo,           // “5분 전” 표시용
        Double distanceKm          // 옵션: 반경 검색 시
) {}