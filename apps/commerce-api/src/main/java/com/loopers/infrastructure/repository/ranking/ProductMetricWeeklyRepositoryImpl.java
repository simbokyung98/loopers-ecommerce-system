package com.loopers.infrastructure.repository.ranking;


import com.loopers.domain.ranking.ProductMetricWeeklyModel;
import com.loopers.domain.ranking.ProductMetricWeeklyRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.loopers.domain.ranking.QProductMetricWeeklyModel.productMetricWeeklyModel;

@RequiredArgsConstructor
@Repository
public class ProductMetricWeeklyRepositoryImpl implements ProductMetricWeeklyRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductMetricWeeklyModel> findAllByPaging(int page, int size, LocalDate startDate, LocalDate endDate) {

        PageRequest pageRequest = PageRequest.of(page, size);

        List<ProductMetricWeeklyModel> productMetricWeeklyModelList = jpaQueryFactory.selectFrom(productMetricWeeklyModel)
                .where(
                        productMetricWeeklyModel.weekStart.eq(startDate),
                        productMetricWeeklyModel.weekEnd.eq(endDate)
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch()
                .stream()
                .toList();

        Long totalCount = jpaQueryFactory.select(productMetricWeeklyModel.count())
                .from(productMetricWeeklyModel)
                .where(
                        productMetricWeeklyModel.weekStart.eq(startDate),
                        productMetricWeeklyModel.weekEnd.eq(endDate)
                )
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;
        return new PageImpl<>(productMetricWeeklyModelList, pageRequest, total);
    }
}
