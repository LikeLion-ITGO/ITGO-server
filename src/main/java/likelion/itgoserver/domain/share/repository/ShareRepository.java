package likelion.itgoserver.domain.share.repository;

import jakarta.persistence.LockModeType;
import likelion.itgoserver.domain.share.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Long>, ShareRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 동시 confirm 안전
    @Query("select s from Share s where s.id = :id")
    Optional<Share> findByIdForUpdate(@Param("id") Long id);
}