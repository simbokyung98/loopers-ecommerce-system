package com.loopers.application.ranking;


import com.loopers.cache.ranking.RankingViewCache;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.ProductMetricMonthlyModel;
import com.loopers.domain.ranking.ProductMetricWeeklyModel;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RankingViewFacade {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductService productService;
    private final BrandService brandService;
    private final RankingViewCache rankingViewCache;
    private final RankingService rankingService;

    public RankingViewInfo.ProductDailyList getTodayTopProductsWithCache(RankingViewCriteria.SearchTodayRanking criteria) {
        // 캐시 계층에 위임
        return rankingViewCache.dailyGetOrLoad(criteria, () -> getTodayTopProducts(criteria));
    }

    /**
     * 캐시에 없을 때만 실행되는 "로더"
     */
    public RankingViewInfo.ProductDailyList getTodayTopProducts(RankingViewCriteria.SearchTodayRanking criteria) {
        String key = getKey(criteria.date());

        int start = (criteria.page() - 1) * criteria.size();
        int end = start + criteria.size() - 1;

        Set<ZSetOperations.TypedTuple<String>> rows =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);

        List<RankingViewInfo.Product> products = new ArrayList<>();

        if (rows != null && !rows.isEmpty()) {
            List<Long> productIds = rows.stream()
                    .map(t -> Long.valueOf(t.getValue()))
                    .toList();

            List<ProductModel> productModels = productService.getListByIds(productIds);

            Map<Long, ProductModel> productMap = productModels.stream()
                    .collect(Collectors.toMap(ProductModel::getId, p -> p));

            List<Long> brandIds = productModels.stream()
                    .map(ProductModel::getBrandId)
                    .distinct()
                    .toList();

            Map<Long, BrandModel> brandModelMap = brandService.getBrandMapByIds(brandIds);

            AtomicInteger offset = new AtomicInteger(start + 1);
            products = rows.stream()
                    .map(item -> {
                        Long productId = Long.valueOf(item.getValue());
                        int rank = offset.getAndIncrement();

                        ProductModel product = productMap.get(productId);
                        BrandModel brand = (product != null) ? brandModelMap.get(product.getBrandId()) : null;

                        return RankingViewInfo.Product.from(product, brand, rank);
                    })
                    .toList();
        }

        return new RankingViewInfo.ProductDailyList(
                criteria.page(),
                criteria.size(),
                criteria.date(),
                products
        );
    }

    private String getKey(LocalDate localDate) {
        return "rank:all:" + localDate;
    }

    // ✅ 주간
    public RankingViewInfo.ProductWeeklyList getWeeklyRankingWithPage(RankingViewCriteria.SearchWeeklyRanking criteria) {
        Page<ProductMetricWeeklyModel> page = rankingService.getWeeklyListWithPage(criteria.toCommand());

        List<RankingViewInfo.Product> products = new ArrayList<>();
        if (page.hasContent()) {
            List<ProductModel> productModels = productService.getListByIds(
                    page.getContent().stream().map(ProductMetricWeeklyModel::getProductId).toList()
            );

            Map<Long, ProductModel> productMap = toProductMap(productModels);
            Map<Long, BrandModel> brandMap = toBrandMap(productModels);

            products = page.getContent().stream()
                    .map(item -> RankingViewInfo.Product.from(
                            productMap.get(item.getProductId()),
                            brandMap.getOrDefault(productMap.get(item.getProductId()).getBrandId(), null),
                            item.getRankValue()
                    ))
                    .toList();
        }

        return new RankingViewInfo.ProductWeeklyList(
                criteria.page(),
                criteria.size(),
                criteria.startDate(),
                criteria.endDate(),
                products
        );
    }

    // ✅ 월간
    public RankingViewInfo.ProductMonthlyList getMonthlyRankingWithPage(RankingViewCriteria.SearchMonthlyRanking criteria) {
        Page<ProductMetricMonthlyModel> page = rankingService.getMonthlyListWithPage(criteria.toCommand());

        List<RankingViewInfo.Product> products = new ArrayList<>();
        if (page.hasContent()) {
            List<ProductModel> productModels = productService.getListByIds(
                    page.getContent().stream().map(ProductMetricMonthlyModel::getProductId).toList()
            );

            Map<Long, ProductModel> productMap = toProductMap(productModels);
            Map<Long, BrandModel> brandMap = toBrandMap(productModels);

            products = page.getContent().stream()
                    .map(item -> RankingViewInfo.Product.from(
                            productMap.get(item.getProductId()),
                            brandMap.getOrDefault(productMap.get(item.getProductId()).getBrandId(), null),
                            item.getRankValue()
                    ))
                    .toList();
        }

        return new RankingViewInfo.ProductMonthlyList(
                criteria.page(),
                criteria.size(),
                criteria.startDate(),
                criteria.endDate(),
                products
        );
    }


    private Map<Long, ProductModel> toProductMap(List<ProductModel> productModels) {
        return productModels.stream().collect(Collectors.toMap(ProductModel::getId, p -> p));
    }

    private Map<Long, BrandModel> toBrandMap(List<ProductModel> productModels) {
        List<Long> brandIds = productModels.stream()
                .map(ProductModel::getBrandId)
                .distinct()
                .toList();
        return brandService.getBrandMapByIds(brandIds);
    }
}
