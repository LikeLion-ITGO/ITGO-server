package likelion.itgoserver.domain.trade.service;

import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;
import likelion.itgoserver.domain.claim.repository.ClaimRepository;
import likelion.itgoserver.domain.image.repository.ShareImageRepository;
import likelion.itgoserver.domain.share.entity.ShareImage;
import likelion.itgoserver.domain.share.repository.ShareRepository;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.trade.dto.TradeDetailResponse;
import likelion.itgoserver.domain.trade.entity.Trade;
import likelion.itgoserver.domain.trade.entity.TradeStatus;
import likelion.itgoserver.domain.trade.repository.TradeRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final ClaimRepository claimRepository;
    private final ShareRepository shareRepository;
    private final ShareImageRepository shareImageRepository;
    private final PublicUrlResolver publicUrlResolver;

    /**
     * Claim 수락 직후 호출해 Trade 생성
     */
    @Transactional
    public void createFromAcceptedClaim(Claim claim) {
        // 1. 동일 claim으로 이미 Trade가 있으면 반환
        tradeRepository.findByClaimId(claim.getId()).orElseGet(() -> {
            // 2) 대표 이미지 키 스냅샷(seq=0)
            String primaryKey = shareImageRepository
                    .findByShareIdAndSeq(claim.getShare().getId(), 0)
                    .map(ShareImage::getObjectKey)
                    .orElse(null);

            Trade trade = Trade.fromAcceptedClaim(claim, primaryKey);
            try {
                return tradeRepository.save(trade);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // uk_trade_claim 충돌(동시 생성) → 이미 생성된 거래를 다시 조회해 반환
                return tradeRepository.findByClaimId(claim.getId()).orElseThrow(() -> e);
            }
        });
    }

    /** 상세 조회 */
    @Transactional(readOnly = true)
    public TradeDetailResponse get(Long tradeId) {
        Trade t = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "trade가 존재하지 않습니다. id=" + tradeId));

        var giver = t.getGiverStore();
        var receiver = t.getReceiverStore();

        String imageUrl = (t.getPrimaryImageKey() != null)
                ? publicUrlResolver.toUrl(t.getPrimaryImageKey())
                : null;

        return new TradeDetailResponse(
                t.getId(),
                imageUrl,
                t.getItemName(),
                t.getBrand(),
                t.getQuantity(),
                t.getExpirationDate(),
                new TradeDetailResponse.StoreInfo(
                        giver.getId(), publicUrlResolver.toUrl(giver.getStoreImageKey()),
                        giver.getStoreName(),
                        giver.getAddress().getRoadAddress(),
                        giver.getOpenTime(), giver.getCloseTime(),
                        giver.getPhoneNumber()
                ),
                new TradeDetailResponse.StoreInfo(
                        receiver.getId(), publicUrlResolver.toUrl(receiver.getStoreImageKey()),
                        receiver.getStoreName(),
                        receiver.getAddress().getRoadAddress(),
                        receiver.getOpenTime(), receiver.getCloseTime(),
                        receiver.getPhoneNumber()
                ),
                t.getStatus().name(),
                t.getRegDate(),
                t.getCompletedAt()
        );
    }

    /** 나눔 완료 */
    @Transactional
    public TradeDetailResponse complete(Long tradeId) {
        Trade t = tradeRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "trade가 존재하지 않습니다. id=" + tradeId));
        if (t.getStatus() == TradeStatus.MATCHED) {
            t.complete();
            t.getWish().close();
        }

        // 나눔 횟수 업데이트
        Store giver = t.getGiverStore();
        Store receiver = t.getReceiverStore();
        giver.increaseGiveTimes();
        receiver.increaseReceivedTimes();

        return get(tradeId);
    }

    /** 나눔 취소 */
    @Transactional
    public TradeDetailResponse cancel(Long tradeId) {
        // Trade 잠금
        Trade trade = tradeRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "trade가 존재하지 않습니다. id=" + tradeId));

        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "이미 나눔이 완료되어 취소할 수 없습니다.");
        }
        if (trade.getStatus() == TradeStatus.CANCELED) {
            return get(tradeId); // 멱등
        }

        // Claim 잠금
        var claim = claimRepository.findByIdForUpdate(trade.getClaim().getId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "claim이 존재하지 않습니다. id=" + trade.getClaim().getId()));

        // 재고 복구
        if (claim.getStatus() != ClaimStatus.CANCELED) {
            var share = shareRepository.findByIdForUpdate(trade.getShare().getId())
                    .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "share가 존재하지 않습니다. id=" + trade.getShare().getId()));
            share.increaseQuantity(claim.getQuantity());
            claim.cancel();
        }

        // 거래 취소
        trade.cancel();
        return get(tradeId);
    }
}