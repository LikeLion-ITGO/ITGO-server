package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "거래 신청 생성 요청")
public record ClaimCreateRequest(
        @Schema(description = "재고 요청 게시글 ID (Wish)", example = "10")
        @NotNull Long wishId,

        @Schema(description = "재고 나눔 게시글 ID (Share)", example = "20")
        @NotNull Long shareId
) {}