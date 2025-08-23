package likelion.itgoserver.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreImageCommitFromDraftRequest(
        @NotNull Long storeId,
        @NotBlank String draftKey
) {}