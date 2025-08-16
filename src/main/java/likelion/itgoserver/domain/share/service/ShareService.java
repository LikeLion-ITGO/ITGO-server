package likelion.itgoserver.domain.share.service;

import likelion.itgoserver.domain.image.dto.*;
import likelion.itgoserver.domain.image.service.ShareImageService;
import likelion.itgoserver.domain.share.dto.ShareResponse;
import likelion.itgoserver.domain.share.dto.ShareUpsertRequest;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.share.entity.ShareImage;
import likelion.itgoserver.domain.share.repository.ShareRepository;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final StoreRepository storeRepository;
    private final PublicUrlResolver publicUrlResolver;
    private final ShareImageService shareImageService;

    /**
     * Share 등록 - 이미지는 비어있는 상태로 생성하고,
     * 이후에 /share-images/confirm 경로를 통해 이미지 확정.
     */
    @Transactional
    public ShareResponse create(ShareUpsertRequest req) {
        validateBusinessRules(req);

        // 참조 무결성: Store 존재 확인
        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "store가 존재하지 않습니다. id=" + req.storeId()));

        // 엔티티 생성
        Share share = Share.builder()
                .store(store)
                .itemName(req.itemName())
                .brand(req.brand())
                .quantity(req.quantity())
                .description(req.description())
                .expirationDate(req.expirationDate())
                .storageType(req.storageType())
                .openTime(req.openTime())
                .closeTime(req.closeTime())
                .build();

        // Share 저장
        Share saved = shareRepository.save(share);
        log.info("Share 생성 완료: id={}, storeId={}, itemName={}", saved.getId(), store.getId(), saved.getItemName());

        // 응답 변환 (이미지 비어있거나, 추후 confirm 후 재조회 시 포함)
        return toResponse(saved);
    }

    /**
     * Share 단건 상세 조회
     */
    @Transactional(readOnly = true)
    public ShareResponse get(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + shareId));
        return toResponse(share);
    }

    /**
     * 내부 유틸
     */
    private void validateBusinessRules(ShareUpsertRequest req) {
        if (req.quantity() < 0) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "수량은 0 이상이어야 합니다.");
        }

        LocalDate exp = req.expirationDate();
        if (exp != null && exp.isBefore(LocalDate.now())) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "유통기한이 이미 지났습니다.");
        }
    }

    private ShareResponse toResponse(Share s) {
        List<ShareImageResponse> images = s.getImages().stream()
                .sorted(Comparator.comparingInt(ShareImage::getSeq)) // 대표는 seq=0
                .map(img -> new ShareImageResponse(
                        img.getSeq(),
                        img.getObjectKey(),
                        publicUrlResolver.toUrl(img.getObjectKey())
                ))
                .toList();

        return new ShareResponse(
                s.getId(),
                s.getItemName(),
                s.getBrand(),
                s.getQuantity(),
                s.getDescription(),
                s.getExpirationDate(),
                s.getStorageType(),
                s.getOpenTime(),
                s.getCloseTime(),
                images,
                s.getStore().getAddress().getRoadAddress()
        );
    }
}