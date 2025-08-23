package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ShareImageDraftPresignResponse(
        Long memberId,
        List<Item> items
) {
    @Schema(description = "presign 응답 항목")
    public record Item(
            Integer seq,
            String putUrl,
            String previewUrl,
            String draftKey
    ) {}
}