package likelion.itgoserver.domain.share.repository;

import likelion.itgoserver.domain.share.dto.ShareWithDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShareRepositoryCustom {
    Page<ShareWithDistance> findMatchesForWish(Long wishId, Pageable pageable);
}