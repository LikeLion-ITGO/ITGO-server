package likelion.itgoserver.global.infra.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicUrlResolver {

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;

    /** DB에 저장한 objectKey를 publicURL로 변환 */
    public String toUrl(String objectKey) {
        if (isBlank(objectKey)) return null;
        if (isAbsoluteUrl(objectKey)) return objectKey;

        String key = trimSlashes(objectKey);
        String cdn = (cdnBaseUrl == null) ? "" : cdnBaseUrl.trim();

        if (!isBlank(cdn)) {
            String base = removeTrailingSlash(cdnBaseUrl);
            return base + "/" + key;
        }
        return s3.getUrl(bucket, key).toString();
    }

    /**
     * 내부 메서드
     */
    private static boolean isAbsoluteUrl(String s) {
        String t = s.toLowerCase(java.util.Locale.ROOT);
        return t.startsWith("http://") || t.startsWith("https://");
    }

    private static String trimSlashes(String s) {
        int start = 0, end = s.length();
        while (start < end && s.charAt(start) == '/') start++;
        while (end > start && s.charAt(end - 1) == '/') end--;
        return s.substring(start, end);
    }

    private static String removeTrailingSlash(String s) {
        return (s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}