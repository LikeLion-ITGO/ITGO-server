package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이미지 확정 후 응답(최종 상태)")
public record ShareImageListResponse(
        @Schema(example = "10") Long shareId,
        List<ShareImageResponse> images
) {}