package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "이미지 확정(커밋) 요청")
public record ShareImageConfirmRequest(
        @Schema(example = "10") @NotNull Long shareId,

        @Schema(description = "확정할 이미지들(최대 5장)")
        @NotEmpty @Size(max = 5)
        List<@Valid ConfirmItem> items
) {
    @Schema(description = "확정 항목")
    public record ConfirmItem(
            @Schema(example = "0") @NotNull @Min(0) @Max(4) Integer seq,
            @Schema(example = "shares/10/images/0_8f2b...jpg") @NotBlank String objectKey
    ) {}

    @AssertTrue(message = "items의 seq는 0~4 범위 내에서 중복 없이 지정되어야 합니다.")
    public boolean isSeqsValid() {
        if (items == null || items.isEmpty()) return false;
        Set<Integer> set = new HashSet<>();
        for (ConfirmItem it : items) {
            if (it == null || it.seq == null) return false;
            int s = it.seq;
            if (s < 0 || s > 4) return false;
            if (!set.add(s)) return false;
        }
        return true;
    }
}