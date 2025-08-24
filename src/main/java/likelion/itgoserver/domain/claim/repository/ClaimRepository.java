package likelion.itgoserver.domain.claim.repository;

import jakarta.persistence.LockModeType;
import likelion.itgoserver.domain.claim.entity.Claim;
import likelion.itgoserver.domain.claim.entity.ClaimStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 특정 나눔에 대한 '거래 신청'을 관리
 */
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByWishIdAndShareId(Long wishId, Long shareId);

    @EntityGraph(attributePaths = {"wish.store", "share.store"}) // 보낸 요청에서 양쪽 store 필요
    Slice<Claim> findByWishId(Long wishId, Pageable pageable);

    @EntityGraph(attributePaths = {"wish.store"}) // 받은 요청에서 wish.store 필요
    Slice<Claim> findByShareId(Long shareId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Claim c where c.id = :id")
    Optional<Claim> findByIdForUpdate(@Param("id") Long id);

    boolean existsByShareIdAndStatusIn(Long shareId, List<ClaimStatus> statusList);

    interface ShareClaimCount {
        Long getShareId();
        long getCnt();
    }

    @Query("""
           select c.share.id as shareId, count(c) as cnt
           from Claim c
           where c.share.id in :shareIds
           group by c.share.id
           """)
    List<ShareClaimCount> countByShareIdInGroup(@Param("shareIds") List<Long> shareIds);

    interface WishClaimCount {
        Long getWishId();
        long getCnt();
    }

    @Query("""
           select c.wish.id as wishId, count(c) as cnt
           from Claim c
           where c.wish.id in :wishIds
           group by c.wish.id
           """)
    List<WishClaimCount> countByWishIdInGroup(@Param("wishIds") List<Long> wishIds);

    @Query("""
        select (count(c) > 0) from Claim c
        where c.share.id = :shareId
          and c.wish.store.id = :storeId
          and c.status in :statuses
    """)
    boolean existsActiveForStoreAndShare(@Param("storeId") Long storeId,
                                         @Param("shareId") Long shareId,
                                         @Param("statuses") Collection<ClaimStatus> statuses);
}