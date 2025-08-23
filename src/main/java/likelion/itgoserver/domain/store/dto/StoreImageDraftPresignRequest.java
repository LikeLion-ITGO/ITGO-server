package likelion.itgoserver.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record StoreImageDraftPresignRequest(
        @NotBlank @Pattern(regexp="^[A-Za-z0-9]+$")
        @Schema(example = "jpeg")
        String ext,

        @NotBlank @Pattern(regexp="^image\\/(jpeg|png|webp|gif)$")
        @Schema(example = "image/jpeg")
        String contentType,

        @PositiveOrZero
        @Schema(example = "6568")
        Long sizeBytes
) {}