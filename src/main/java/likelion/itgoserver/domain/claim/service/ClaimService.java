package likelion.itgoserver.domain.claim.service;

import likelion.itgoserver.domain.claim.dto.ClaimResponse;
import likelion.itgoserver.domain.claim.dto.ReceivedClaimItem;
import likelion.itgoserver.domain.claim.dto.SentClaimItem;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.share.entity.ShareImage;
import likelion.itgoserver.domain.share.repository.ShareRepository;
import likelion.itgoserver.domain.claim.repository.ClaimRepository;
import likelion.itgoserver.domain.trade.service.TradeService;
import likelion.itgoserver.domain.wish.entity.Wish;
import likelion.itgoserver.domain.wish.repository.WishRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ShareRepository shareRepository;
    private final ShareImageRepository shareImageRepository;
    private final ClaimRepository claimRepository;
    private final WishRepository wishRepository;
    private final PublicUrlResolver publicUrlResolver;
    private final TradeService tradeService;

    /**
     * 요청 생성(PENDING)
     * - 같은 (wish, share) 조합 중복 요청은 uk_wish_share로 차단
     * - 요청 수량은 1 이상
     */
    @Transactional
    public ClaimResponse request(Long wishId, Long shareId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "wish가 존재하지 않습니다. id=" + wishId));

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + shareId));

        Claim claim = Claim.from(wish, share);

        try {
            claim = claimRepository.save(claim);
        } catch (DataIntegrityViolationException e) {
            // uk_wish_share 위반 → 이미 같은 상대에게 요청함
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미 해당 나눔글에 요청을 보냈습니다.");
        }

        log.info("Claim 생성: id={}, wish={}, share={}, qty={}", claim.getId(), wishId, shareId, wish.getQuantity());
        return ClaimResponse.from(claim);
    }

    /**
     * 요청 수락
     * - Claim이 PENDING일 때만
     * - Share 재고 차감 (비관잠금)
     */
    @Transactional
    public ClaimResponse accept(Long claimId) {
        Claim claim = claimRepository.findByIdForUpdate(claimId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "claim이 존재하지 않습니다. id=" + claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            return ClaimResponse.from(claim); // 이미 처리된 경우 그대로 반환
        }

        Share share = shareRepository.findByIdForUpdate(claim.getShare().getId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + claim.getShare().getId()));

        // 수량 차감
        share.decreaseQuantity(claim.getQuantity());

        // 상태 갱신
        claim.accept();

        // 3) 거래 생성
        tradeService.createFromAcceptedClaim(claim);

        log.info("Claim 수락: id={}, shareId={}, remainQty={}", claimId, share.getId(), share.getQuantity());
        return ClaimResponse.from(claim);
    }

    /**
     * 요청 거절
     * - PENDING일 때만 상태 전환
     */
    @Transactional
    public ClaimResponse reject(Long claimId) {
        Claim claim = claimRepository.findByIdForUpdate(claimId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "claim이 존재하지 않습니다. id=" + claimId));

        if (claim.getStatus() == ClaimStatus.PENDING) {
            claim.reject();
            log.info("Claim 거절: id={}", claimId);
        }
        return ClaimResponse.from(claim);
    }

    /**
     * 요청 취소 : 요청자 측
     * - PENDING일 때만 상태 전환
     */
    @Transactional
    public ClaimResponse cancel(Long claimId) {
        Claim claim = claimRepository.findByIdForUpdate(claimId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "claim이 존재하지 않습니다. id=" + claimId));

        if (claim.getStatus() == ClaimStatus.PENDING) {
            claim.cancel();
            log.info("Claim 취소: id={}", claimId);
        }
        return ClaimResponse.from(claim);
    }

    @Transactional(readOnly = true)
    public Page<ReceivedClaimItem> receivedByShare(Long shareId, Pageable pageable) {
        Page<Claim> page = claimRepository.findByShareId(shareId, pageable);
        return page.map(c -> {
            var w = c.getWish();
            var st = w.getStore();
            return new ReceivedClaimItem(
                    c.getId(),
                    w.getId(),
                    new ReceivedClaimItem.StoreSummary(st.getId(), st.getStoreName()),
                    w.getTitle(),
                    w.getDescription(),
                    c.getRegDate(),
                    c.getStatus()
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<SentClaimItem> sentByWish(Long wishId, Pageable pageable) {
        Page<Claim> page = claimRepository.findByWishId(wishId, pageable);

        // 1) 필요한 shareIds 수집
        var shareIds = page.getContent().stream()
                .map(c -> c.getShare().getId())
                .distinct()
                .toList();

        // 2) 대표 이미지(seq=0) 배치 조회 → Map<shareId, objectKey>
        Map<Long, String> primaryKeyByShareId = shareImageRepository
                .findByShareIdInAndSeq(shareIds, 0)
                .stream()
                .sorted(Comparator.comparingInt(ShareImage::getSeq))
                .collect(Collectors.toMap(
                        si -> si.getShare().getId(),
                        ShareImage::getObjectKey,
                        (a, b) -> a // 혹시 중복 시 첫번째 유지
                ));

        // 3) 매핑
        return page.map(c -> {
            var s = c.getShare();
            var wishStore = c.getWish().getStore();
            var shareStore = s.getStore();

            String primaryUrl = null;
            String key = primaryKeyByShareId.get(s.getId());
            if (key != null) primaryUrl = publicUrlResolver.toUrl(key);

            Double distanceKm = round1(distanceKm(
                    wishStore.getAddress().getLatitude(),
                    wishStore.getAddress().getLongitude(),
                    shareStore.getAddress().getLatitude(),
                    shareStore.getAddress().getLongitude()
            ));

            return new SentClaimItem(
                    c.getId(),
                    s.getId(),
                    primaryUrl,
                    s.getBrand(),
                    s.getItemName(),
                    s.getQuantity(),
                    s.getOpenTime(),
                    s.getCloseTime(),
                    s.getExpirationDate(),
                    distanceKm,
                    c.getRegDate(),
                    c.getStatus()
            );
        });
    }

    /**
     * 내부 유틸
     */
    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0088; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private static Double round1(Double v) {
        if (v == null) return null;
        return Math.round(v * 10.0) / 10.0;
    }

}