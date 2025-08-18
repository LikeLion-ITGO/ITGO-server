package likelion.itgoserver.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import likelion.itgoserver.domain.image.dto.*;
import likelion.itgoserver.domain.image.dto.ShareImageConfirmRequest.ConfirmItem;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareImageService {

    private final ShareRepository shareRepository;
    private final ShareImageRepository shareImageRepository;
    private final PublicUrlResolver publicUrlResolver;
    private final S3ImageService s3ImageService;

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
    public ShareImagePresignResponse presign(Long shareId, ShareImagePresignRequest request) {
        if (!shareRepository.existsById(shareId)) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + shareId);
        }

        if (request.items() == null || request.items().isEmpty())
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "요청 이미지가 없습니다.");
        if (request.items().size() > MAX_IMAGES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지는 최대 " + MAX_IMAGES + "장까지");

        for (var it : request.items()) {
            if (it.sizeBytes() != null && it.sizeBytes() > MAX_IMAGE_BYTES) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 최대 크기(10MB)를 초과했습니다. seq=" + it.seq());
            }
            if (!ALLOWED_IMAGE_MIME.contains(it.contentType())) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "허용되지 않는 이미지 타입입니다. seq=" + it.seq());
            }
        }

        var ttl = java.time.Duration.ofMinutes(presignTtlMinutes);
        var items = request.items().stream()
                .map(it -> {
                    // shares/{shareId}/images/{seq}_{uuid}.{ext}
                    String key = s3ImageService.shareKey(shareId, it.seq(), it.ext());

                    // 프런트의 PUT에도 동일한 Content-Type 헤더
                    String putUrl = s3ImageService.createPresignedPutUrl(key, it.contentType(), ttl).toString();

                    // 업로드 직후 미리보기용
                    String publicUrl = publicUrlResolver.toUrl(key);

                    return new ShareImagePresignResponse.PresignItemResponse(it.seq(), putUrl, key, publicUrl);
                })
                .toList();
        return new ShareImagePresignResponse(shareId, items);
    }

    @Transactional
    public ShareImageListResponse confirm(ShareImageConfirmRequest req) {
        final Long shareId = req.shareId();

        if (req.items() == null) throw new CustomException(GlobalErrorCode.BAD_REQUEST, "요청 이미지가 없습니다.");
        if (req.items().size() > MAX_IMAGES)
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지는 최대 " + MAX_IMAGES + "장까지");

        // Share 잠금
        Share share = shareRepository.findByIdForUpdate(shareId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + shareId));

        // 기존 키 백업
        List<String> oldKeys = share.getImages().stream()
                .map(ShareImage::getObjectKey)
                .toList();

        // 입력 검증 + 새 엔티티 준비
        String expectedPrefix = "shares/" + shareId + "/images/";
        var seenSeq = new java.util.HashSet<Integer>();
        var nextImages = new java.util.ArrayList<ShareImage>(req.items().size());

        for (ConfirmItem item : req.items()) {
            // seq 중복 방지
            if (!seenSeq.add(item.seq())) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미지 seq가 중복되었습니다. seq=" + item.seq());
            }

            String objectKey = item.objectKey();

            // 키 prefix 검증
            if (!objectKey.startsWith(expectedPrefix)) {
                throw new CustomException(GlobalErrorCode.BAD_REQUEST,
                        "objectKey가 허용된 경로가 아닙니다. requiredPrefix=" + expectedPrefix);
            }

            // S3 HEAD 검증 (존재 + 용량 + MIME)
            ObjectMetadata md = headObject(objectKey);
            validateMetadata(md, objectKey);

            // 새 엔티티 생성 + 연관관계 매핑
            ShareImage created = ShareImage.builder()
                    .seq(item.seq())
                    .objectKey(objectKey)
                    .build();
            nextImages.add(created);
        }

        // 기존 이미지 제거 + DELETE 반영
        share.getImages().clear();
        shareRepository.flush();

        // 새 이미지 추가
        nextImages.forEach(share::addImage);

        // 저장
        shareRepository.saveAndFlush(share);

        // S3 고아 객체 정리
        var keepKeys = nextImages.stream().map(ShareImage::getObjectKey).collect(java.util.stream.Collectors.toSet());
        var toDelete = oldKeys.stream()
                .filter(k -> !keepKeys.contains(k))
                .filter(k -> k.startsWith(expectedPrefix))
                .toList();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                for (String key : toDelete) {
                    try {
                        if (key != null && !key.isBlank()) s3ImageService.deleteObject(key);
                    }
                    catch (Exception e) {
                        log.warn("Orphan image delete failed. shareId={}, key={}", shareId, key, e);
                    }
                }
            }
        });

        return buildResponse(shareId);
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

    private ShareImageListResponse buildResponse(Long shareId) {
        List<ShareImage> list = shareImageRepository.findByShareIdOrderBySeqAsc(shareId);
        List<ShareImageResponse> images = list.stream()
                .map(img -> new ShareImageResponse(
                        img.getSeq(),
                        img.getObjectKey(),
                        publicUrlResolver.toUrl(img.getObjectKey())
                ))
                .collect(Collectors.toList());
        return new ShareImageListResponse(shareId, images);
    }

}