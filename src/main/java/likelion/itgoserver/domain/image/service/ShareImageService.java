package likelion.itgoserver.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import likelion.itgoserver.domain.image.dto.*;
import likelion.itgoserver.domain.share.dto.ShareUpsertRequest.ImageDraftItem;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.share.entity.ShareImage;
import likelion.itgoserver.domain.share.repository.ShareRepository;
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

import java.util.List;
import java.util.Set;

import static org.springframework.transaction.support.TransactionSynchronizationManager.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareImageService {

    private final ShareRepository shareRepository;
    private final S3ImageService s3ImageService;
    private final PublicUrlResolver publicUrlResolver;

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.image.presign-ttl-minutes:15}")
    private long presignTtlMinutes;

    /** 허용 이미지 최대 크기 : 10MB */
    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_IMAGES = 5;

    /** 허용 MIME 목록(서버 측 방어) */
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Transactional(readOnly = true)
    public ShareImageDraftPresignResponse presignDraft(Long memberId, ShareImageDraftPresignRequest request) {
        if (request.items() == null || request.items().isEmpty())
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "요청 이미지가 없습니다.");
        if (request.items().size() > MAX_IMAGES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지는 최대 5장까지");

        for (var it : request.items()) {
            if (it.sizeBytes() != null && it.sizeBytes() > MAX_IMAGE_BYTES) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 최대 크기(10MB) 초과: seq=" + it.seq());
            }
            if (!ALLOWED_IMAGE_MIME.contains(it.contentType())) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "허용되지 않는 타입: seq=" + it.seq());
            }
        }

        var ttl = java.time.Duration.ofMinutes(presignTtlMinutes);
        var items = request.items().stream()
                .map(it -> {
                    String draftKey = s3ImageService.draftKey(memberId, it.seq(), it.ext());
                    String putUrl   = s3ImageService.createPresignedPutUrl(draftKey, it.contentType(), ttl).toString();
                    String previewUrl = publicUrlResolver.toUrl(draftKey);
                    return new ShareImageDraftPresignResponse.Item(it.seq(), putUrl, previewUrl, draftKey);
                })
                .toList();

        return new ShareImageDraftPresignResponse(memberId, items);
    }

    @Transactional
    public void commitFromDraft(Long memberId, Share share, List<ImageDraftItem> images) {
        if (images == null) return;
        if (images.size() > MAX_IMAGES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지는 최대 5장까지");

        // 1. 유효성 & 목적지 키 계산 & 즉시 복사 수행
        String draftPrefix = "drafts/" + memberId + "/images/";
        var seenSeq   = new java.util.HashSet<Integer>();
        var destImgs  = new java.util.ArrayList<ShareImage>(images.size());
        var destKeys  = new java.util.ArrayList<String>(images.size()); // 롤백 시 삭제용
        for (var it : images) {
            if (it == null || it.seq() == null || it.draftKey() == null)
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 항목이 올바르지 않습니다.");
            int seq = it.seq();
            if (seq < 0 || seq > 4 || !seenSeq.add(seq))
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "seq는 0~4 범위 내 중복 없이 지정");

            String draftKey = it.draftKey();
            if (!draftKey.startsWith(draftPrefix)) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "다른 사용자의 draftKey 접근은 불가");
            }

            // HEAD 검증
            ObjectMetadata md = headObject(draftKey);
            validateMetadata(md, draftKey);

            // 목적지 키 계산 & 즉시 복사
            String ext = S3ImageService.extractExtFromKey(draftKey);
            String finalKey = s3ImageService.shareKey(share.getId(), seq, ext);
            s3ImageService.copyObject(draftKey, finalKey);
            destKeys.add(finalKey);

            // 엔티티 준비
            destImgs.add(ShareImage.builder()
                    .share(share)
                    .seq(seq)
                    .objectKey(finalKey)
                    .build());
        }

        // 2. 기존 키 백업 후 교체
        List<String> oldKeys = share.getImages().stream().map(ShareImage::getObjectKey).toList();
        share.getImages().clear();
        destImgs.forEach(share::addImage);
        shareRepository.saveAndFlush(share);

        // 3. 트랜잭션 : 커밋 시 draft 삭제 / 롤백 시 방금 복사한 dest 제거
        var toDeleteDrafts = images.stream().map(ImageDraftItem::draftKey).toList();
        var keepKeys = destImgs.stream().map(ShareImage::getObjectKey).collect(java.util.stream.Collectors.toSet());
        var toDeleteOld = oldKeys.stream().filter(k -> !keepKeys.contains(k)).toList();

        registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        if (status == STATUS_COMMITTED) {
                            // 커밋 성공: draft 제거 + 옛 final 제거
                            safeDeleteAll(toDeleteDrafts);
                            safeDeleteAll(toDeleteOld);
                        } else {
                            // 롤백: 방금 복사한 목적지 제거
                            safeDeleteAll(destKeys);
                        }
                    }
                });
    }

    /**
     * 내부 유틸
     */
    private ObjectMetadata headObject(String objectKey) {
        try {
            return s3.getObjectMetadata(new GetObjectMetadataRequest(bucket, objectKey));
        } catch (Exception e) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST,
                    "S3에 해당 objectKey가 존재하지 않습니다. key=" + objectKey);
        }
    }

    /** 용량/타입 서버 측 검증 */
    private void validateMetadata(ObjectMetadata md, String objectKey) {
        long size = md.getContentLength();
        if (size <= 0) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST,
                    "업로드된 이미지 크기가 0입니다. key=" + objectKey);
        }
        if (size > MAX_IMAGE_BYTES) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST,
                    "최대 허용 크기(10MB)를 초과했습니다. key=" + objectKey);
        }

        String ctRaw = java.util.Optional.ofNullable(md.getContentType()).orElse("").toLowerCase(java.util.Locale.ROOT);
        String ct = ctRaw.split(";", 2)[0].trim();
        if (!ALLOWED_IMAGE_MIME.contains(ct)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST,
                    "허용되지 않는 이미지 MIME 타입입니다. contentType=" + ctRaw);
        }
    }

    private void safeDeleteAll(java.util.Collection<String> keys) {
        if (keys == null) return;
        for (String k : keys) {
            try { if (k != null && !k.isBlank()) s3ImageService.deleteObject(k); }
            catch (Exception e) { log.warn("S3 삭제 실패 key={}", k, e); }
        }
    }

}