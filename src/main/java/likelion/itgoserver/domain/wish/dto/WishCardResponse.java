package likelion.itgoserver.domain.wish.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자가 요청한 Wish 카드")
public record WishCardResponse(
        Long wishId,
        String title,
        String itemName,
        String brand,
        Integer quantity,
        String description,
        LocalDateTime regDate,
        Integer claimTotalCount
) {
}