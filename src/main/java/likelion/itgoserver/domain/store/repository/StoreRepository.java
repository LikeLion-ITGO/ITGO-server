package likelion.itgoserver.domain.store.repository;

import jakarta.persistence.LockModeType;
import likelion.itgoserver.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query("select s.id from Store s where s.owner.id = :memberId")
    Optional<Long> findIdByOwnerId(@Param("memberId") Long memberId);

    Optional<Store> findByOwnerId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Store s where s.id = :id")
    Optional<Store> findByIdForUpdate(@Param("id") Long id);
}