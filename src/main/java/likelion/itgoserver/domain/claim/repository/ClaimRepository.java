package likelion.itgoserver.domain.claim.repository;

import jakarta.persistence.LockModeType;
import likelion.itgoserver.domain.claim.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 특정 나눔에 대한 '거래 신청'을 관리
 */
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByWishIdAndShareId(Long wishId, Long shareId);

    @EntityGraph(attributePaths = {"wish.store", "share.store"}) // 보낸 요청에서 양쪽 store 필요
    Page<Claim> findByWishId(Long wishId, Pageable pageable);

    @EntityGraph(attributePaths = {"wish.store"}) // 받은 요청에서 wish.store 필요
    Page<Claim> findByShareId(Long shareId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Claim c where c.id = :id")
    Optional<Claim> findByIdForUpdate(@Param("id") Long id);
}