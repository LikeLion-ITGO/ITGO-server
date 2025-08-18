package likelion.itgoserver.domain.trade.service;

import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.domain.trade.dto.TradeGivenItem;
import likelion.itgoserver.domain.trade.dto.TradeReceivedItem;
import likelion.itgoserver.domain.trade.entity.Trade;
import likelion.itgoserver.domain.trade.entity.TradeStatus;
import likelion.itgoserver.domain.trade.repository.TradeRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeListService {

    private final StoreRepository storeRepository;
    private final TradeRepository tradeRepository;

    /** 나눔한 내역 조회 */
    @Transactional(readOnly = true)
    public Page<TradeGivenItem> listGiven(Long memberId, TradeStatus status, Pageable pageable) {
        Long storeId = storeRepository.findIdByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 회원의 매장이 없습니다."));

        Page<Trade> page = (status == null)
                ? tradeRepository.findByGiverStoreId(storeId, pageable)
                : tradeRepository.findByGiverStoreIdAndStatus(storeId, status, pageable);

        return page.map(t -> {
            var share = t.getShare();
            var wish = t.getWish();
            var giver = t.getGiverStore();
            var receiver = t.getReceiverStore();
            Double distance = round1(distanceKm(giver, receiver));

            return new TradeGivenItem(
                    t.getId(),
                    t.getStatus().name(),
                    share.getBrand(),
                    share.getItemName(),
                    share.getOpenTime(),
                    share.getCloseTime(),
                    share.getExpirationDate(),
                    wish.getQuantity(),
                    distance,
                    receiver.getAddress().getRoadAddress(),
                    receiver.getOpenTime(),
                    receiver.getCloseTime(),
                    receiver.getPhoneNumber()
            );
        });
    }

    /** 나눔받은 내역 조회 */
    @Transactional(readOnly = true)
    public Page<TradeReceivedItem> listReceived(Long memberId, TradeStatus status, Pageable pageable) {
        Long storeId = storeRepository.findIdByOwnerId(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 회원의 매장이 없습니다."));

        Page<Trade> page = (status == null)
                ? tradeRepository.findByReceiverStoreId(storeId, pageable)
                : tradeRepository.findByReceiverStoreIdAndStatus(storeId, status, pageable);

        return page.map(t -> {
            var share = t.getShare();
            var wish = t.getWish();
            var giver = t.getGiverStore();
            var receiver = t.getReceiverStore();
            Double distance = round1(distanceKm(giver, receiver));

            return new TradeReceivedItem(
                    t.getId(),
                    t.getStatus().name(),
                    share.getBrand(),
                    share.getItemName(),
                    share.getOpenTime(),
                    share.getCloseTime(),
                    share.getExpirationDate(),
                    wish.getQuantity(),
                    distance,
                    giver.getAddress().getRoadAddress(),
                    giver.getOpenTime(),
                    giver.getCloseTime(),
                    giver.getPhoneNumber()
            );
        });
    }

    private static double distanceKm(Store a, Store b) {
        final double R = 6371.0088; // km
        double lat1 = a.getAddress().getLatitude();
        double lon1 = a.getAddress().getLongitude();
        double lat2 = b.getAddress().getLatitude();
        double lon2 = b.getAddress().getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double va = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(va), Math.sqrt(1 - va));
        return R * c;
    }

    private static Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}