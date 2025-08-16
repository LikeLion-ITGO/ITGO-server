package likelion.itgoserver.domain.wish.repository;

import likelion.itgoserver.domain.wish.entity.Wish;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * '필요 물품 요청' 글을 관리
 */
public interface WishRepository extends JpaRepository<Wish, Long> {
}