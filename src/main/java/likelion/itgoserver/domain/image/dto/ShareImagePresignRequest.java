package likelion.itgoserver.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

@Schema(description = "이미지 업로드 presign 요청(여러 장)")
public record ShareImagePresignRequest(
        @Schema(description = "업로드할 항목들(최대 5장 권장)")
        @NotEmpty @Size(max = 5)
        List<@Valid PresignItemRequest> items
) {
    @Schema(description = "이미지 업로드 presign 요청 항목")
    public record PresignItemRequest(
            @Schema(description = "시퀀스(고정 자리)", example = "0")
            @NotNull @Min(0) @Max(4) Integer seq,

            @Schema(description = "확장자", example = "jpg")
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9]+$") String ext,

            @Schema(description = "MIME 타입", example = "image/jpeg")
            @NotBlank @Pattern(regexp = "^image\\/(jpeg|png|webp|gif)$") String contentType,

            @Schema(description = "파일 크기 바이트(선택, 서버/정책 검증용)", example = "5242880")
            @PositiveOrZero Long sizeBytes
    ) {}

    @AssertTrue(message = "items의 seq는 0~4 범위 내에서 중복 없이 지정되어야 합니다.")
    public boolean isSeqsValid() {
        if (items == null) return false;
        Set<Integer> set = new java.util.HashSet<>();
        for (var it : items) {
            if (it == null || it.seq() == null) return false;
            int s = it.seq();
            if (s < 0 || s > 4) return false;
            if (!set.add(s)) return false;
        }
        return true;
    }
}