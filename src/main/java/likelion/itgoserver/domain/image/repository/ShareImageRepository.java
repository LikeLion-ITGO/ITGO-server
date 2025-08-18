package likelion.itgoserver.domain.image.repository;

import likelion.itgoserver.domain.share.entity.ShareImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareImageRepository extends JpaRepository<ShareImage, Long> {
    Optional<ShareImage> findByShareIdAndSeq(Long shareId, Integer seq);
    List<ShareImage> findByShareIdOrderBySeqAsc(Long shareId);
    List<ShareImage> findByShareIdInAndSeq(List<Long> shareIds, Integer seq);
}