package likelion.itgoserver.domain.share.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import likelion.itgoserver.domain.share.entity.StorageType;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "공유 글(Share) 생성/수정 요청")
public record ShareUpsertRequest(
        @Schema(description = "상품명", example = "초코파이")
        @NotBlank String itemName,

        @Schema(description = "브랜드", example = "오리온")
        String brand,

        @Schema(description = "수량(0 이상)", example = "10")
        @NotNull @Min(0) Integer quantity,

        @Schema(description = "설명", example = "유통기한 임박, 미개봉 새 상품")
        @Size(max = 500) String description,

        @Schema(description = "유통기한(선택)", example = "2025-09-30")
        LocalDate expirationDate,

        @Schema(description = "보관 유형", example = "REFRIGERATED")
        @NotNull StorageType storageType,

        @Schema(description = "신선식품 인증 여부", example = "false")
        Boolean freshCertified,

        @Schema(description = "오픈 시각", example = "09:00")
        @NotNull LocalTime openTime,

        @Schema(description = "마감 시각", example = "18:00")
        @NotNull LocalTime closeTime
) {}
