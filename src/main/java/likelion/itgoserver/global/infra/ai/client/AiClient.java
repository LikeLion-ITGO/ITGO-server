package likelion.itgoserver.global.infra.ai.client;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import likelion.itgoserver.global.infra.ai.dto.FreshnessResponse;
import likelion.itgoserver.global.infra.ai.dto.LabelExtractResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AiClient {

    private final RestClient rest;

    public AiClient(RestClient aiRestClient) {
        this.rest = aiRestClient;
    }

    /** MultipartFile -> Resource (파일명 보존) */
    private Resource toResource(MultipartFile f) throws IOException {
        return new ByteArrayResource(f.getBytes()) {
            @Override public String getFilename() { return Objects.requireNonNullElse(f.getOriginalFilename(), "image.jpg"); }
        };
    }

    /** /labels/extract (OCR+KIE) */
    public LabelExtractResponse extractLabels(List<MultipartFile> files, boolean verbose) throws IOException {
        var body = new LinkedMultiValueMap<String, Object>();
        for (var f : files) {
            Resource r = toResource(f);
            ContentDisposition cd = ContentDisposition.formData()
                    .name("files")
                    .filename(r.getFilename())
                    .build();

            var part = new HttpEntity<>(r, new HttpHeaders() {{
                setContentDisposition(cd);
                setContentType(MediaType.parseMediaType(
                        f.getContentType() != null ? f.getContentType() : MediaType.IMAGE_JPEG_VALUE));
            }});
            body.add("files", part);
        }

        return rest.post()
                .uri(uri -> uri.path("/labels/extract")
                        .queryParam("verbose", verbose ? "1" : "0")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) ->
                { throw new RuntimeException("AI error: " + res.getStatusCode() + " " + res.getBody()); })
                .body(LabelExtractResponse.class);
    }

    /** /freshness/classify */
    public FreshnessResponse classifyFreshness(List<MultipartFile> files, String pre, boolean timing, boolean debug) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (var f : files) {
            body.add("files", new HttpEntity<>(toResource(f)));
        }
        return rest.post()
                .uri(uri -> uri.path("/freshness/classify")
                        .queryParam("pre", pre == null ? "none" : pre)
                        .queryParam("timing", timing ? "1" : "0")
                        .queryParam("debug", debug ? "1" : "0")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(FreshnessResponse.class);
    }
}