package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "드래프트에서 최종 이미지로 커밋")
public record ShareImageCommitFromDraftRequest(
        @NotNull Long shareId,
        @NotEmpty @Size(max = 5)
        List<@Valid Item> items
) {
    public record Item(
            @NotNull @Min(0) @Max(4) Integer seq,
            @NotBlank String draftKey
    ) {}

    @AssertTrue(message = "items의 seq는 0~4 범위 내에서 중복 없이 지정되어야 합니다.")
    public boolean isSeqsValid() {
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