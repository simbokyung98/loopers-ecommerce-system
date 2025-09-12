package com.loopers.cache.product;

import com.loopers.cache.CachePolicy;
import com.loopers.interfaces.api.product.OrderType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;

@Component
public class ProductCachePolicyRegistry {
    private final EnumMap<OrderType, CachePolicy> map = new EnumMap<>(OrderType.class);

    public ProductCachePolicyRegistry() {
        // 좋아요 높은순 정렬만 refreshAhead + prewarmPages 적용
        map.put(OrderType.높은좋아요순, CachePolicy.of(Duration.ofSeconds(30), 3, Duration.ofSeconds(10), 3));
        map.put(OrderType.최신순, CachePolicy.of(Duration.ofSeconds(30), 3, Duration.ofSeconds(10), 3));
        map.put(OrderType.낮은좋아요순, CachePolicy.of(Duration.ofSeconds(30), 3));
        map.put(OrderType.오래된순, CachePolicy.of(Duration.ofMinutes(10), 5));
        map.put(OrderType.낮은가격순, CachePolicy.of(Duration.ofSeconds(30), 3));
        map.put(OrderType.높은가격순, CachePolicy.of(Duration.ofSeconds(30), 3));
    }

    public CachePolicy get(OrderType t) { return map.get(t); }
}
