package likelion.itgoserver.domain.trade.repository;

import jakarta.persistence.LockModeType;
import likelion.itgoserver.domain.trade.entity.Trade;
import likelion.itgoserver.domain.trade.entity.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Optional<Trade> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trade t where t.id = :id")
    Optional<Trade> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Optional<Trade> findByClaimId(Long claimId);

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Page<Trade> findByGiverStoreId(Long giverStoreId, Pageable pageable);

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Page<Trade> findByGiverStoreIdAndStatus(Long giverStoreId, TradeStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Page<Trade> findByReceiverStoreId(Long receiverStoreId, Pageable pageable);

    @EntityGraph(attributePaths = {"giverStore", "receiverStore", "share", "wish"})
    Page<Trade> findByReceiverStoreIdAndStatus(Long receiverStoreId, TradeStatus status, Pageable pageable);

    interface TradeIdByClaim {
        Long getClaimId();
        Long getTradeId();
    }

    @Query("select t.claim.id as claimId, t.id as tradeId from Trade t where t.claim.id in :claimIds")
    List<TradeIdByClaim> findTradeIdsByClaimIds(@Param("claimIds") List<Long> claimIds);
}