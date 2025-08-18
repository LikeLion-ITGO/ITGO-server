package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 글 이미지 단건 응답")
public record ShareImageResponse(
        @Schema(example = "0") Integer seq,
        @Schema(example = "shares/10/images/0_8f2b...jpg") String objectKey,
        @Schema(description = "퍼블릭 URL(CDN/S3)", example = "https://cdn.example.com/shares/10/images/0_8f2b...jpg")
        String publicUrl
) {}