package likelion.itgoserver.domain.wish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "필요 재고 요청 및 매칭 신청 DTO")
public record WishUpsertRequest(
        @Schema(description = "요청 제목", example = "초코파이를 나눠주세요")
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "상품명", example = "초코파이")
        @NotBlank @Size(max = 100) String itemName,

        @Schema(description = "브랜드", example = "오리온")
        @Size(max = 100) String brand,

        @Schema(description = "수량", example = "4")
        @NotNull @Min(1) Integer quantity,

        @Schema(description = "설명", example = "배고파요. 간식 주세요!!")
        @Size(max = 500) String description,

        @Schema(description = "오픈 시각", example = "09:00")
        @NotNull LocalTime openTime,

        @Schema(description = "마감 시각", example = "18:00")
        @NotNull LocalTime closeTime
) {
}