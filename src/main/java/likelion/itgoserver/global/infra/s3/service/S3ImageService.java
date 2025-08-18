// S3ImageService.java
package likelion.itgoserver.global.infra.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);
    private static final Duration MAX_TTL = Duration.ofDays(7);

    private static final Set<String> ALLOWED_EXT =
            Set.of("jpg","jpeg","png","webp","gif");

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /** Store: 단일 이미지 키 생성 */
    public String storeKey(Long storeId, String ext) {
        return join("stores", storeId.toString(), "cover", uuid() + "." + cleanExt(ext));
    }

    /** Share: 최대 5장 슬롯용 키 생성 */
    public String shareKey(Long shareId, int seq, String ext) {
        return join("shares", shareId.toString(), "images", seq + "_" + uuid() + "." + cleanExt(ext));
    }

    /** 업로드용 Presigned PUT URL 발급 */
    public URL createPresignedPutUrl(String key, String contentType, Duration ttl) {
        String k = trimSlashes(key);
        Date expiration = Date.from(Instant.now().plus(clampTtl(ttl)));

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, k)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        if (!isBlank(contentType)) {
            req.addRequestParameter("Content-Type", contentType);
        }
        return s3.generatePresignedUrl(req);
    }

    /** 객체 존재 여부 */
    public boolean doesObjectExist(String key) {
        return s3.doesObjectExist(bucket, trimSlashes(key));
    }

    /** 메타데이터 조회 */
    public ObjectMetadata getObjectMetadata(String key) {
        return s3.getObjectMetadata(bucket, trimSlashes(key));
    }

    /** 삭제 */
    public void deleteObject(String key) {
        s3.deleteObject(bucket, trimSlashes(key));
    }

    /**
     * 내부 메서드
     */
    private static String cleanExt(String ext) {
        if (isBlank(ext)) return "jpg";
        String e = ext.trim().toLowerCase(Locale.ROOT);
        if (e.startsWith(".")) e = e.substring(1);
        return ALLOWED_EXT.contains(e) ? e : "jpg";
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (isBlank(p)) continue;
            String seg = trimSlashes(p);
            if (!sb.isEmpty()) sb.append('/');
            sb.append(seg);
        }
        return sb.toString();
    }

    private static String trimSlashes(String s) {
        int start = 0, end = s.length();
        while (start < end && s.charAt(start) == '/') start++;
        while (end > start && s.charAt(end - 1) == '/') end--;
        return s.substring(start, end);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Duration clampTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) return Duration.ofMinutes(15);
        return ttl.compareTo(MAX_TTL) > 0 ? MAX_TTL : ttl;
    }
}
