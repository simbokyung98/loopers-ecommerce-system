package com.loopers.infrastructure.repository.ranking;

import com.loopers.domain.ranking.ProductMetricMonthlyModel;
import com.loopers.domain.ranking.ProductMetricMonthlyRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.loopers.domain.ranking.QProductMetricMonthlyModel.productMetricMonthlyModel;

@RequiredArgsConstructor
@Repository
public class ProductMetricMonthlyRepositoryImpl implements ProductMetricMonthlyRepository {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Page<ProductMetricMonthlyModel> findAllByPaging(int page, int size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<ProductMetricMonthlyModel> productMetricMonthlyModels = jpaQueryFactory.selectFrom(productMetricMonthlyModel)
                .where()
                .where(
                        productMetricMonthlyModel.monthStart.eq(startDate),
                        productMetricMonthlyModel.monthEnd.eq(endDate)
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch()
                .stream()
                .toList();

        Long totalCount = jpaQueryFactory.select(productMetricMonthlyModel.count())
                .from(productMetricMonthlyModel)
                .where(
                        productMetricMonthlyModel.monthStart.eq(startDate),
                        productMetricMonthlyModel.monthEnd.eq(endDate)
                )
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;
        return new PageImpl<>(productMetricMonthlyModels, pageRequest, total);
    }
}
