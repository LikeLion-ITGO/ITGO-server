package likelion.itgoserver.domain.share.service;

import likelion.itgoserver.domain.claim.repository.ClaimRepository;
import likelion.itgoserver.domain.image.dto.*;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
import likelion.itgoserver.domain.image.service.ShareImageService;
import likelion.itgoserver.domain.member.repository.MemberRepository;
import likelion.itgoserver.domain.share.dto.ShareCardResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Order.desc;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final StoreRepository storeRepository;
    private final PublicUrlResolver publicUrlResolver;
    private final ShareImageRepository shareImageRepository;
    private final ClaimRepository claimRepository;

    /**
     * Share 등록 - 이미지는 비어있는 상태로 생성하고,
     * 이후에 /share-images/confirm 경로를 통해 이미지 확정.
     */
    @Transactional
    public ShareResponse create(Long memberId, ShareUpsertRequest req) {
        validateBusinessRules(req);

        // 참조 무결성: Store 존재 확인
        Store store = storeRepository.findByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "회원의 가게가 없습니다."));

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

    public Page<ShareCardResponse> listMyShareCards(Long memberId, Pageable pageable) {
        Long storeId = storeRepository.findIdByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "회원의 가게가 없습니다."));

        // 정렬 강제 고정
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(desc("regDate"), desc("id")));

        Page<Share> page = shareRepository.findByStoreId(storeId, fixed);
        if (page.isEmpty()) return Page.empty(fixed);

        // 이번 페이지 shareIds
        List<Long> shareIds = page.getContent().stream().map(Share::getId).toList();

        // 대표이미지 seq=0 조회 → shareId -> url
        Map<Long, String> primaryUrlByShareId = shareImageRepository
                .findByShareIdInAndSeq(shareIds, 0)
                .stream()
                .sorted(Comparator.comparingInt(ShareImage::getSeq))
                .collect(Collectors.toMap(
                        si -> si.getShare().getId(),
                        si -> publicUrlResolver.toUrl(si.getObjectKey()),
                        (a, b) -> a
                ));

        // 각 Share의 Claim 총 개수
        Map<Long, Integer> countByShareId = claimRepository.countByShareIdInGroup(shareIds)
                .stream()
                .collect(Collectors.toMap(
                        ClaimRepository.ShareClaimCount::getShareId,
                        r -> (int) r.getCnt()
                ));

        return page.map(s -> new ShareCardResponse(
                s.getId(),
                s.getItemName(),
                s.getBrand(),
                s.getQuantity(),
                s.getExpirationDate(),
                s.getOpenTime(),
                s.getCloseTime(),
                s.getRegDate(),
                primaryUrlByShareId.getOrDefault(s.getId(), null),
                countByShareId.getOrDefault(s.getId(), 0)
        ));
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

    private ShareResponse toResponse(Share share) {
        List<ShareImageResponse> images = share.getImages().stream()
                .sorted(Comparator.comparingInt(ShareImage::getSeq)) // 대표는 seq=0
                .map(img -> new ShareImageResponse(
                        img.getSeq(),
                        img.getObjectKey(),
                        publicUrlResolver.toUrl(img.getObjectKey())
                ))
                .toList();

        return new ShareResponse(
                share.getId(),
                share.getItemName(),
                share.getBrand(),
                share.getQuantity(),
                share.getDescription(),
                share.getExpirationDate(),
                share.getStorageType(),
                share.getOpenTime(),
                share.getCloseTime(),
                images,
                share.getStore().getAddress().getRoadAddress(),
                share.getRegDate()
        );
    }

}