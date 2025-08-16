package likelion.itgoserver.domain.wish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "필요 재고 요청 및 매칭 신청 DTO")
public record WishUpsertRequest(
        @NotNull Long storeId,
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 100) String itemName,
        @Size(max = 100) String brand,
        @NotNull @Min(1) Integer quantity,
        @Size(max = 500) String description,
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime
) {
}