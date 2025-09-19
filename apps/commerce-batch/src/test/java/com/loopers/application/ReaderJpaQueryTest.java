package com.loopers.application;

import com.loopers.application.dto.AggregateRow;
import com.loopers.domain.metric.ProductMetricDailyModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EntityScan(basePackages = "com.loopers.domain.metric")
@Transactional
class ReaderJpaQueryTest {

    @PersistenceContext
    EntityManager em;

    @Test
    void groupBy_query_returns_aggregated_rows() {
        // given: 일간 raw 데이터 적재
        LocalDate d1 = LocalDate.of(2025, 9, 10);
        LocalDate d2 = LocalDate.of(2025, 9, 11);

        ProductMetricDailyModel a = new ProductMetricDailyModel(1L, d1);
        a.updateViewCount(10); a.updateLikeCount(2); a.updateSaleCount(1);
        em.persist(a);

        ProductMetricDailyModel b = new ProductMetricDailyModel(1L, d2);
        b.updateViewCount(5); b.updateLikeCount(1); b.updateSaleCount(0);
        em.persist(b);

        ProductMetricDailyModel c = new ProductMetricDailyModel(2L, d1);
        c.updateViewCount(20); c.updateLikeCount(0); c.updateSaleCount(0);
        em.persist(c);

        em.flush(); em.clear();

        // when: 동일 JPQL 실행
        List<AggregateRow> rows = em.createQuery("""
                select new com.loopers.application.dto.AggregateRow(
                    d.productId,
                    sum(d.viewCount),
                    sum(d.likeCount),
                    sum(d.saleCount)
                )
                from ProductMetricDailyModel d
                where d.date between :start and :end
                group by d.productId
                """, AggregateRow.class)
                .setParameter("start", d1)
                .setParameter("end",   d2)
                .getResultList();

        // then
        assertEquals(2, rows.size());
        var r1 = rows.stream().filter(r -> r.productId().equals(1L)).findFirst().orElseThrow();
        assertEquals(15, r1.viewSum()); // 10 + 5
        assertEquals(3,  r1.likeSum()); // 2 + 1
        assertEquals(1,  r1.orderSum()); // 1 + 0
    }
}

