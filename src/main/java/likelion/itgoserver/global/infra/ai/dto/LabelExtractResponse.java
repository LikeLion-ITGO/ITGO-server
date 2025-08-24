package likelion.itgoserver.global.infra.ai.dto;

import java.util.List;

public record LabelExtractResponse(
        Labels labels,
        List<ImageResult> images
) {
    public record Value(
            String value,
            Double confidence,
            List<String> evidence
    ) {}

    public record Labels(
            Value brand,
            Value item_name,
            Value storage
    ) {}

    public record ImageResult(
            String image_id,
            Value brand,
            Value item_name,
            Value storage
    ) {}
}