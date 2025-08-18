package likelion.itgoserver.domain.share.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.TimePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import likelion.itgoserver.domain.share.dto.ShareWithDistance;
import likelion.itgoserver.domain.wish.entity.Wish;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static likelion.itgoserver.domain.share.entity.QShare.share;
import static likelion.itgoserver.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class ShareRepositoryImpl implements ShareRepositoryCustom {

    private final JPAQueryFactory query;
    private final EntityManager em;

    @Override
    public Page<ShareWithDistance> findMatchesForWish(Long wishId, Pageable pageable) {
        Wish w = em.find(Wish.class, wishId);
        if (w == null || !w.isActive()) return Page.empty(pageable);

        BooleanBuilder where = new BooleanBuilder()
                .and(share.store.address.dong.eq(w.getStore().getAddress().getDong()))
                .and(share.itemName.eq(w.getItemName()))
                .and(share.quantity.goe(w.getQuantity()))
                .and(share.quantity.gt(0))
                .and(share.expirationDate.goe(LocalDate.now()))
                .and(overlap(share.openTime, share.closeTime, w.getOpenTime(), w.getCloseTime()));

        // 총 개수
        Long totalBoxed = query.select(share.count())
                .from(share).join(share.store, store)
                .where(where)
                .fetchOne();

        long total = (totalBoxed == null) ? 0L : totalBoxed;
        if (total == 0L) return Page.empty(pageable);

        // 거리 계산 → 선택 컬럼으로 함께 조회
        NumberExpression<Double> distMeters = Expressions.numberTemplate(
                Double.class,
                "ST_Distance_Sphere(point({0}, {1}), point({2}, {3}))",
                share.store.address.longitude, share.store.address.latitude,
                w.getStore().getAddress().getLongitude(), w.getStore().getAddress().getLatitude()
        );

        JPAQuery<Tuple> base = query
                .select(share, distMeters)
                .from(share)
                .join(share.store, store).fetchJoin()
                .where(where)
                .orderBy(distMeters.asc(), share.regDate.desc()); // 가까운 순 + 최신순

        List<Tuple> tuples = base
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ShareWithDistance> content = tuples.stream()
                .map(t -> new ShareWithDistance(
                        t.get(share),
                        // m → km 변환
                        t.get(distMeters) == null ? null : t.get(distMeters) / 1000.0
                ))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression overlap(TimePath<LocalTime> aStart,
                                      TimePath<LocalTime> aEnd,
                                      LocalTime bStart,
                                      LocalTime bEnd) {
        // 당일 범위 전제: [aStart, aEnd] ∩ [bStart, bEnd] ≠ ∅  ⇔ aStart ≤ bEnd && aEnd ≥ bStart
        return aStart.loe(bEnd).and(aEnd.goe(bStart));
    }
}