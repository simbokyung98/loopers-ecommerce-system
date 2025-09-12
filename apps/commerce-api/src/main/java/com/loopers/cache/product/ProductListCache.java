package com.loopers.cache.product;

import com.loopers.application.common.PageInfo;
import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.cache.CachePolicy;
import com.loopers.interfaces.api.product.OrderType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class ProductListCache {

    private final RedisTemplate<String, PageInfo.PageEnvelope<ProductInfo.Product>> productPageRedisTemplate;
    private final ProductCachePolicyRegistry policies;
    private final ProductListKeyBuilder keyBuilder;
    private final ProductLikeVersionService likeVersionService;

    public ProductListCache(
            RedisTemplate<String, PageInfo.PageEnvelope<ProductInfo.Product>> productPageRedisTemplate,
            ProductCachePolicyRegistry policies,
            ProductListKeyBuilder keyBuilder,
            ProductLikeVersionService likeVersionService
    ) {
        this.productPageRedisTemplate = productPageRedisTemplate;
        this.policies = policies;
        this.keyBuilder = keyBuilder;
        this.likeVersionService = likeVersionService;
    }

    public PageInfo.PageEnvelope<ProductInfo.Product> getOrLoad(
            ProductCriteria.SearchProducts criteria,
            Supplier<PageInfo.PageEnvelope<ProductInfo.Product>> loader
    ) {
        OrderType orderType = criteria.orderType();
        CachePolicy policy = policies.get(orderType);

        // 정책상 캐시하지 않는 페이지는 바로 로더
        if (criteria.page() > policy.maxPageInclusive()) {
            return loader.get();
        }

        // 키 만들기: 메인(높은좋아요순)만 전역 버전 포함
        final String key;
        if (orderType == OrderType.높은좋아요순) {
            String ver = likeVersionService.current();
            key = String.format(
                    "prod:list:%s:%s:g%s:p%d:s%d",
                    orderType.name(),
                    (criteria.brandId() == null ? "all" : String.valueOf(criteria.brandId())),
                    ver, criteria.page(), criteria.size()
            );
        } else {
            key = keyBuilder.build(orderType, criteria.brandId(), criteria.page(), criteria.size());
        }

        // 1) 히트
        PageInfo.PageEnvelope<ProductInfo.Product> cached =
                productPageRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 메인만 refresh-ahead
            if (orderType == OrderType.높은좋아요순) {
                Long remain = productPageRedisTemplate.getExpire(key); // seconds
                if (policy.shouldRefreshAhead(remain)) {
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            PageInfo.PageEnvelope<ProductInfo.Product> fresh = loader.get();
                            productPageRedisTemplate.opsForValue().set(key, fresh, policy.ttl());
                        } catch (Exception ignored) {}
                    });
                }
            }
            return cached;
        }

        // 2) 미스 → 로더 실행 후 저장
        PageInfo.PageEnvelope<ProductInfo.Product> fresh = loader.get();
        productPageRedisTemplate.opsForValue().set(key, fresh, policy.ttl());
        return fresh;
    }
}
