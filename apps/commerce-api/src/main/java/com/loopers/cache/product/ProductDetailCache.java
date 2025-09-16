package com.loopers.cache.product;


import com.loopers.application.product.dto.ProductInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class ProductDetailCache {

    private static final Duration TTL = Duration.ofMinutes(2);

    private final RedisTemplate<String, ProductInfo.Product> detailRedis;
    private final ProductDetailKeyBuilder keyBuilder;

    public ProductDetailCache(
            @Qualifier("productDetailRedisTemplate")
            RedisTemplate<String, ProductInfo.Product> detailRedis,
            ProductDetailKeyBuilder keyBuilder
    ) {
        this.detailRedis = detailRedis;
        this.keyBuilder = keyBuilder;
    }

    public ProductInfo.Product getOrLoad(Long productId, Supplier<ProductInfo.Product> loader) {
        String key = keyBuilder.key(productId);

        ProductInfo.Product cached = detailRedis.opsForValue().get(key);
        if (cached != null) return cached;

        ProductInfo.Product fresh = loader.get();
        detailRedis.opsForValue().set(key, fresh, TTL);
        return fresh;
    }

    public void evict(Long productId) {
        detailRedis.delete(keyBuilder.key(productId));
    }
}
