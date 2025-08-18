package likelion.itgoserver.domain.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;

import java.time.LocalDateTime;

@Schema(description = "최종 거래 응답 DTO")
public record ClaimResponse(
        @Schema(description = "생성된 거래 신청의 고유 ID", example = "1")
        Long claimId,

        @Schema(description = "재고 요청 게시글 ID", example = "1")
        Long wishId,

        @Schema(description = "재고 나눔 게시글 ID", example = "1")
        Long shareId,

        @Schema(description = "요청한 가게 ID", example = "1")
        Long requesterStoreId,

        @Schema(description = "요청한 수량", example = "2")
        Integer quantity,

        @Schema(description = "요청 상태 (PENDING, ACCEPTED, REJECTED)", example = "PENDING")
        ClaimStatus status,

        @Schema(description = "요청이 온 시간")
        LocalDateTime claimAt,

        @Schema(description = "수락/거절/취소가 결정된 시각")
        LocalDateTime decidedAt
) {
        public static ClaimResponse from(Claim claim) {
                return new ClaimResponse(
                        claim.getId(),
                        claim.getWish().getId(),
                        claim.getShare().getId(),
                        claim.getRequesterStore().getId(),
                        claim.getQuantity(),
                        claim.getStatus(),
                        claim.getRegDate(),
                        claim.getDecidedAt()
                );
        }
}