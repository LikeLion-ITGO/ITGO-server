package likelion.itgoserver.global.infra.ai.dto;

import java.util.List;
import java.util.Map;

public record FreshnessResponse(
        String final_label,
        List<String> results,
        int count,
        List<Map<String, Double>> probs,
        String pre_mode,
        String layout,
        Map<String, Double> timing_sec
) {}