package likelion.itgoserver.domain.share.repository;

import likelion.itgoserver.domain.share.dto.ShareWithDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ShareRepositoryCustom {
    Page<ShareWithDistance> findMatchesForWish(Long wishId, Pageable pageable);

    Page<ShareWithDistance> findActiveByDong(
            String dong,
            Double originLat,
            Double originLng,
            Long originStoreId,
            LocalDate today,
            Pageable pageable
    );
}