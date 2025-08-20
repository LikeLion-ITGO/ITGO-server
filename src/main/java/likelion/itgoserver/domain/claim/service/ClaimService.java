package likelion.itgoserver.domain.claim.service;

import likelion.itgoserver.domain.claim.dto.ClaimResponse;
import likelion.itgoserver.domain.claim.dto.ReceivedClaimItem;
import likelion.itgoserver.domain.claim.dto.ReceivedClaimItem.StoreSummary;
import likelion.itgoserver.domain.claim.dto.ReceivedClaimItem.WishSummary;
import likelion.itgoserver.domain.claim.dto.SentClaimItem;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
import likelion.itgoserver.domain.share.entity.Share;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.share.entity.ShareImage;
import likelion.itgoserver.domain.share.repository.ShareRepository;
import likelion.itgoserver.domain.claim.repository.ClaimRepository;
import likelion.itgoserver.domain.trade.repository.TradeRepository;
import likelion.itgoserver.domain.trade.service.TradeService;
import likelion.itgoserver.domain.wish.entity.Wish;
import likelion.itgoserver.domain.wish.repository.WishRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static likelion.itgoserver.domain.trade.repository.TradeRepository.*;

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
    private final TradeRepository tradeRepository;

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

        // 재고 부족이면 자동 거절
        if (share.getQuantity() < claim.getQuantity()) {
            claim.reject(); // decidedAt 세팅됨
            log.info("재고 부족으로 자동 거절: claimId={}, shareId={}", claimId, share.getId());
            return ClaimResponse.from(claim);
        }

        // 수량 차감
        share.decreaseQuantity(claim.getQuantity());
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

        // 이미 끝난 상태는 멱등 처리
        if (claim.getStatus() == ClaimStatus.CANCELED || claim.getStatus() == ClaimStatus.REJECTED) {
            return ClaimResponse.from(claim);
        }

        // PENDING -> 단순 취소
        if (claim.getStatus() == ClaimStatus.PENDING) {
            claim.cancel();
            log.info("Claim 취소(PENDING): id={}", claimId);
            return ClaimResponse.from(claim);
        }

        // ACCEPTED 상태는 Trade에서만 취소
        if (claim.getStatus() == ClaimStatus.ACCEPTED) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "ACCEPTED Claim은 거래 내역(Trad)에서 취소 가능");
        }

        return ClaimResponse.from(claim);
    }

    @Transactional(readOnly = true)
    public Slice<ReceivedClaimItem> receivedByShare(Long shareId, Pageable pageable) {
        // 정렬
        Pageable fixed = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id"))
        );

        // 요청 조회
        Slice<Claim> slice = claimRepository.findByShareId(shareId, fixed);
        if (slice.isEmpty()) {
            return new SliceImpl<>(List.of(), fixed, slice.hasNext());
        }

        // tradeId 매핑
        List<Long> claimIds = slice.getContent().stream().map(Claim::getId).toList();
        Map<Long, Long> tradeIdByClaimId = tradeRepository.findTradeIdsByClaimIds(claimIds).stream()
                .collect(Collectors.toMap(TradeIdByClaim::getClaimId, TradeIdByClaim::getTradeId));

        // DTO 매핑
        return slice.map(c -> {
            var w = c.getWish();
            var st = w.getStore();
            Long tradeId = tradeIdByClaimId.get(c.getId()); // ACCEPTED면 존재, 아니면 null
            return new ReceivedClaimItem(
                    c.getId(),
                    tradeId,
                    c.getStatus(),
                    c.getRegDate(),
                    new WishSummary(w.getId(), w.getTitle(), w.getDescription()),
                    new StoreSummary(st.getId(), st.getStoreName())
            );
        });
    }

    @Transactional(readOnly = true)
    public Slice<SentClaimItem> sentByWish(Long wishId, Pageable pageable) {
        // 정렬
        Pageable fixed = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id")));

        // 요청 조회
        Slice<Claim> slice = claimRepository.findByWishId(wishId, fixed);
        if (slice.isEmpty()) {
            return new SliceImpl<>(List.of(), fixed, slice.hasNext());
        }

        // 필요한 shareIds 수집
        List<Claim> claims = slice.getContent();
        List<Long> shareIds = claims.stream().map(c -> c.getShare().getId()).distinct().toList();
        List<Long> claimIds = claims.stream().map(Claim::getId).toList();

        // 대표 이미지 seq=0 → url 매핑
        Map<Long, String> primaryUrlByShareId = shareImageRepository
                .findByShareIdInAndSeq(shareIds, 0)
                .stream()
                .sorted(Comparator.comparingInt(ShareImage::getSeq))
                .collect(Collectors.toMap(
                        si -> si.getShare().getId(),
                        si -> publicUrlResolver.toUrl(si.getObjectKey()),
                        (a, b) -> a
                ));

        // tradeId 배치 조회
        Map<Long, Long> tradeIdByClaimId = tradeRepository.findTradeIdsByClaimIds(claimIds)
                .stream()
                .collect(Collectors.toMap(
                        TradeIdByClaim::getClaimId,
                        TradeIdByClaim::getTradeId
                ));

        // 3) 매핑
        return slice.map(c -> {
            var share = c.getShare();
            var wishStore = c.getWish().getStore();
            var shareStore = share.getStore();

            Double distanceKm = round1(distanceKm(
                    wishStore.getAddress().getLatitude(),
                    wishStore.getAddress().getLongitude(),
                    shareStore.getAddress().getLatitude(),
                    shareStore.getAddress().getLongitude()
            ));

            Long tradeId = tradeIdByClaimId.get(c.getId());
            String primaryImageUrl = primaryUrlByShareId.get(share.getId());
            return new SentClaimItem(
                    c.getId(),
                    tradeId,
                    c.getStatus(),
                    c.getRegDate(),
                    new SentClaimItem.ShareSummary(
                            share.getId(),
                            primaryImageUrl,
                            share.getBrand(),
                            share.getItemName(),
                            share.getQuantity(),
                            share.getOpenTime(),
                            share.getCloseTime(),
                            share.getExpirationDate()
                    ),
                    distanceKm
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