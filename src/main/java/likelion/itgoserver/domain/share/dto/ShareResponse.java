package likelion.itgoserver.domain.share.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.image.dto.ShareImageResponse;
import likelion.itgoserver.domain.share.entity.StorageType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "공유 글(Share) 단건 응답")
public record ShareResponse(
        @Schema(example = "10") Long shareId,
        String itemName,
        String brand,
        Integer quantity,
        String description,
        LocalDate expirationDate,
        StorageType storageType,
        LocalTime openTime,
        LocalTime closeTime,
        @Schema(description = "이미지 목록(확정 후)", implementation = ShareImageResponse.class)
        List<ShareImageResponse> images,
        String roadAddress
) {
}