package likelion.itgoserver.domain.wish.service;

import likelion.itgoserver.domain.claim.repository.ClaimRepository;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
import likelion.itgoserver.domain.share.dto.ShareWithDistance;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.share.repository.ShareRepository;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.domain.wish.dto.WishCardResponse;
import likelion.itgoserver.domain.wish.dto.WishCreateAndMatchResponse;
import likelion.itgoserver.domain.wish.dto.WishMatchItem;
import likelion.itgoserver.domain.wish.dto.WishUpsertRequest;
import likelion.itgoserver.domain.wish.entity.Wish;
import likelion.itgoserver.domain.wish.repository.WishRepository;
import likelion.itgoserver.domain.store.entity.Store;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final StoreRepository storeRepository;
    private final ShareRepository shareRepository;
    private final ShareImageRepository shareImageRepository;
    private final ClaimRepository claimRepository;
    private final PublicUrlResolver publicUrlResolver;

    @Transactional
    public WishCreateAndMatchResponse createAndMatch(Long memberId, WishUpsertRequest req, Pageable pageable) {
        // 1. 기본 검증
        if (!req.openTime().isBefore(req.closeTime())) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "openTime은 closeTime보다 앞서야 합니다.");
        }

        // 2. store 확인
        Store store = storeRepository.findByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "store가 존재하지 않습니다."));

        // 3. Wish 저장
        Wish wish = Wish.builder()
                .store(store)
                .title(req.title())
                .itemName(req.itemName())
                .brand(req.brand())
                .quantity(req.quantity())
                .description(req.description())
                .openTime(req.openTime())
                .closeTime(req.closeTime())
                .build();
        wish = wishRepository.save(wish);

        // 4) 매칭 쿼리
        var page = shareRepository.findMatchesForWish(wish.getId(), pageable);

        List<Long> shareIds = page.getContent().stream()
                .map(swd -> swd.share().getId())
                .toList();

        Map<Long, String> primaryUrlByShareId = shareImageRepository
                .findByShareIdInAndSeq(shareIds, 0)
                .stream()
                .collect(Collectors.toMap(
                        si -> si.getShare().getId(),
                        si -> publicUrlResolver.toUrl(si.getObjectKey()),
                        (a, b) -> a
                ));

        // 5) DTO 매핑
        var items = page.stream()
                .map(swd -> toMatchItem(swd, primaryUrlByShareId.get(swd.share().getId())))
                .toList();

        return new WishCreateAndMatchResponse(wish.getId(), items);
    }

    public Page<WishCardResponse> listMyWishCards(Long memberId, Pageable pageable) {
        Long storeId = storeRepository.findIdByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "회원의 가게가 없습니다."));

        // 정렬 강제 고정
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id")));

        Page<Wish> page = wishRepository.findByStoreId(storeId, fixed);
        if (page.isEmpty()) return Page.empty(fixed);

        List<Long> wishIds = page.getContent().stream().map(Wish::getId).toList();

        Map<Long, Integer> countByWishId = claimRepository.countByWishIdInGroup(wishIds)
                .stream()
                .collect(Collectors.toMap(
                        ClaimRepository.WishClaimCount::getWishId,
                        r -> (int) r.getCnt()
                ));

        return page.map(w -> new WishCardResponse(
                w.getId(),
                w.getTitle(),
                w.getItemName(),
                w.getBrand(),
                w.getQuantity(),
                w.getDescription(),
                w.getRegDate(),
                countByWishId.getOrDefault(w.getId(), 0)
        ));
    }

    @Transactional(readOnly = true)
    public Page<WishCardResponse> listMyActiveWishCards(Long memberId, Pageable pageable) {
        Long storeId = storeRepository.findIdByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "회원의 가게가 없습니다."));

        // 정렬 강제 고정
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id")));

        Page<Wish> page = wishRepository.findByStoreIdAndIsActiveTrue(storeId, fixed);
        if (page.isEmpty()) return Page.empty(fixed);

        List<Long> wishIds = page.getContent().stream().map(Wish::getId).toList();

        Map<Long, Integer> countByWishId = claimRepository.countByWishIdInGroup(wishIds)
                .stream()
                .collect(Collectors.toMap(
                        ClaimRepository.WishClaimCount::getWishId,
                        r -> (int) r.getCnt()
                ));

        return page.map(w -> new WishCardResponse(
                w.getId(),
                w.getTitle(),
                w.getItemName(),
                w.getBrand(),
                w.getQuantity(),
                w.getDescription(),
                w.getRegDate(),
                countByWishId.getOrDefault(w.getId(), 0)
        ));
    }

    private WishMatchItem toMatchItem(ShareWithDistance swd, String primaryUrl) {
        Share share = swd.share();

        Long minutesAgo = (share.getRegDate() == null) ? null :
                ChronoUnit.MINUTES.between(share.getRegDate(), LocalDateTime.now());

        // 소수점 1자리 반올림
        Double kmRounded = null;
        if (swd.distanceKm() != null) {
            kmRounded = Math.round(swd.distanceKm() * 10.0) / 10.0;
        }

        return new WishMatchItem(
                share.getId(),
                share.getItemName(),
                share.getBrand(),
                share.getQuantity(),
                share.getExpirationDate(),
                share.getStorageType(),
                share.getOpenTime(),
                share.getCloseTime(),
                primaryUrl,
                minutesAgo,
                kmRounded
        );
    }
}