package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class StoreImageDtos {

    @Schema(description = "가게 이미지 Presigned URL 발급 요청")
    public record StoreImagePresignRequest(
            @Schema(description = "확장자", example = "jpg")
            String ext,

            @Schema(description = "HTTP 파일 형식", example = "image/jpeg")
            String contentType
    ) {}

    @Schema(description = "Presigned URL 발급 응답")
    public record PresignResponse(
            String putUrl,      // 프론트가 직접 업로드할 S3 URL
            String objectKey,   // DB에 저장할 S3 Key
            String publicUrl    // 미리보기 (서버에서 Key를 바탕으로 URL로 변환)
    ) {}

    @Schema(description = "가게 이미지 업로드 확정 요청")
    public record StoreImageConfirmRequest(
            Long storeId,
            String objectKey
    ) {}
}