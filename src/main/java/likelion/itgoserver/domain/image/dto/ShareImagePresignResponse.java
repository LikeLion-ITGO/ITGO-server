package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이미지 업로드 presign 응답")
public record ShareImagePresignResponse(
        @Schema(example = "10") Long shareId,
        @Schema(description = "각 항목별 PUT URL과 오브젝트 키")
        List<PresignItemResponse> items
) {
    @Schema(description = "presign 응답 항목")
    public record PresignItemResponse(
            @Schema(example = "0") Integer seq,
            @Schema(description = "S3 PUT presigned URL") String putUrl,
            @Schema(description = "S3 Object Key", example = "shares/10/images/0_8f2b...jpg") String objectKey,
            @Schema(description = "퍼블릭 접근 URL(선택). 업로드 직후 미리보기용", example = "https://cdn.example.com/shares/10/images/0_8f2b...jpg")
            String publicUrl
    ) {}
}