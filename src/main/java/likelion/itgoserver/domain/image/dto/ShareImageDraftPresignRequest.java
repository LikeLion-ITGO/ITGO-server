package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "드래프트 이미지 업로드 presign 요청")
public record ShareImageDraftPresignRequest(
        @NotEmpty @Size(max = 5)
        List<@Valid PresignItem> items
) {
    public record PresignItem(
            @NotNull @Min(0) @Max(4)
            @Schema(example = "0")
            Integer seq,

            @NotBlank @Pattern(regexp = "^[A-Za-z0-9]+$")
            @Schema(example = "jpg")
            String ext,

            @NotBlank @Pattern(regexp = "^image\\/(jpeg|png|webp|gif)$")
            @Schema(example = "image/jpeg")
            String contentType,

            @PositiveOrZero
            @Schema(example = "65518")
            Long sizeBytes
    ) {}

    @AssertTrue(message = "items의 seq는 0~4 범위 내에서 중복 없이 지정되어야 합니다.")
    public boolean isSeqsValid() {
        if (items == null) return false;
        Set<Integer> set = new HashSet<>();
        for (var it : items) {
            if (it == null || it.seq() == null) return false;
            int s = it.seq();
            if (s < 0 || s > 4) return false;
            if (!set.add(s)) return false;
        }
        return true;
    }
}