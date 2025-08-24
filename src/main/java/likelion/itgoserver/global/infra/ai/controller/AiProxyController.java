package likelion.itgoserver.global.infra.ai.controller;

import likelion.itgoserver.global.infra.ai.client.AiClient;
import likelion.itgoserver.global.infra.ai.dto.FreshnessResponse;
import likelion.itgoserver.global.infra.ai.dto.LabelExtractResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai-proxy")
public class AiProxyController {

    private final AiClient ai;

    public AiProxyController(AiClient ai) { this.ai = ai; }

    @PostMapping("/labels")
    public LabelExtractResponse labels(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "0") int verbose
    ) throws IOException {
        return ai.extractLabels(files, verbose == 1);
    }

    @PostMapping("/freshness")
    public FreshnessResponse freshness(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "none") String pre,
            @RequestParam(defaultValue = "0") int timing,
            @RequestParam(defaultValue = "0") int debug
    ) throws IOException {
        return ai.classifyFreshness(files, pre, timing == 1, debug == 1);
    }
}