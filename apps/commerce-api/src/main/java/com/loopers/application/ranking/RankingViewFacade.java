package com.loopers.application.ranking;


import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
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


    public RankingViewInfo.ProductList getTodayTopProducts(RankingViewCriteria.SearchTodayRanking criteria) {
        String key = getKey(criteria.date());

        int start = (criteria.page() -1) * criteria.size();
        int end = start + criteria.size() - 1;

        Set<ZSetOperations.TypedTuple<String>> rows =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);

        List<RankingViewInfo.Product> products = new ArrayList<>();


        if (!rows.isEmpty()){
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

            Map<Long, BrandModel> brandModelMap =
                    brandService.getBrandMapByIds(brandIds);

            AtomicInteger offset = new AtomicInteger(start + 1);
            products = rows.stream()
                    .map(item -> {
                        Long productId = Long.valueOf(item.getValue());
                        int rank = offset.getAndIncrement();

                        ProductModel product = productMap.get(productId);
                        BrandModel brand = product != null ? brandModelMap.get(product.getBrandId()) : null;

                        return RankingViewInfo.Product.from(product, brand, rank);
                    })
                    .toList();
        }

        return new RankingViewInfo.ProductList(
                criteria.page(),
                criteria.size(),
                criteria.date(),
                products
                );
    }

    private String getKey(LocalDate localDate) {
        return "rank:all:" + localDate;
    }

}
