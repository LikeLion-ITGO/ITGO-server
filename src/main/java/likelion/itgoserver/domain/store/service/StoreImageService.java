package likelion.itgoserver.domain.store.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import likelion.itgoserver.domain.store.dto.StoreImageDraftPresignRequest;
import likelion.itgoserver.domain.store.dto.StoreImageDraftPresignResponse;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import likelion.itgoserver.global.infra.s3.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.Set;

import static java.util.Locale.*;
import static java.util.Optional.ofNullable;
import static org.springframework.transaction.support.TransactionSynchronizationManager.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StoreImageService {

    private final StoreRepository storeRepository;
    private final S3ImageService s3ImageService;
    private final PublicUrlResolver publicUrlResolver;

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.image.presign-ttl-minutes:15}")
    private long presignTtlMinutes;

    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("image/jpeg","image/png","image/webp","image/gif");

    @Transactional(readOnly = true)
    public StoreImageDraftPresignResponse presignDraft(Long memberId, StoreImageDraftPresignRequest req) {
        if (req.sizeBytes() != null && req.sizeBytes() > MAX_IMAGE_BYTES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 최대 10MB 초과");
        if (!ALLOWED.contains(req.contentType()))
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "허용되지 않는 이미지 타입");

        var ttl = java.time.Duration.ofMinutes(presignTtlMinutes);
        String draftKey = s3ImageService.storeDraftKey(memberId, req.ext());
        String putUrl   = s3ImageService.createPresignedPutUrl(draftKey, req.contentType(), ttl).toString();
        String previewUrl = publicUrlResolver.toUrl(draftKey);
        return new StoreImageDraftPresignResponse(memberId, putUrl, previewUrl, draftKey);
    }

    @Transactional
    public void commitFromDraft(Long memberId, Long storeId, String draftKey) {
        Store store = storeRepository.findByIdForUpdate(storeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "store가 존재하지 않습니다. id=" + storeId));
        if (!store.getOwner().getId().equals(memberId))
            throw new CustomException(GlobalErrorCode.INVALID_PERMISSION, "가게의 소유자가 아닙니다.");

        String expectedPrefix = "drafts/" + memberId + "/store/image/";
        if (!draftKey.startsWith(expectedPrefix))
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "다른 사용자의 draftKey입니다.");

        var md = s3.getObjectMetadata(new GetObjectMetadataRequest(bucket, draftKey));
        validate(md, draftKey);

        String ext = S3ImageService.extractExtFromKey(draftKey);
        String finalKey = s3ImageService.storeImageKey(storeId, ext);
        s3ImageService.copyObject(draftKey, finalKey);

        String oldKey = store.getStoreImageKey();
        store.updateImageKey(finalKey);
        storeRepository.saveAndFlush(store);

        registerSynchronization(
                new TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        if (status == STATUS_COMMITTED) { safeDelete(draftKey); safeDelete(oldKey); }
                        else { safeDelete(finalKey); }
                    }
                }
        );
    }

    private void validate(ObjectMetadata md, String key) {
        long size = md.getContentLength();
        if (size <= 0 || size > MAX_IMAGE_BYTES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 용량 오류: " + key);
        String ctRaw = ofNullable(md.getContentType()).orElse("").toLowerCase(ROOT);
        String ct = ctRaw.split(";", 2)[0].trim();
        if (!ALLOWED.contains(ct))
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "허용되지 않는 MIME: " + ctRaw);
    }

    private void safeDelete(String key) {
        try { if (key != null && !key.isBlank()) s3ImageService.deleteObject(key); }
        catch (Exception e) { log.warn("S3 삭제 실패 key={}", key, e); }
    }
}
