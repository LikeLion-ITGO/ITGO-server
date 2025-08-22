package likelion.itgoserver.domain.wish.repository;

import likelion.itgoserver.domain.wish.entity.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<Wish, Long> {
    Page<Wish> findByStoreId(Long storeId, Pageable pageable);

    Page<Wish> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);
}