package likelion.itgoserver.domain.store.dto;

public record StoreImageDraftPresignResponse(
        Long memberId,
        String putUrl,
        String previewUrl,
        String draftKey
) {}
