package com.loopers.application.product;

import com.loopers.application.product.dto.ProductInfo;
import com.loopers.cache.product.ProductDetailCache;
import com.loopers.cache.product.ProductDetailKeyBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductDetailCacheTest {

    @Mock
    RedisTemplate<String, ProductInfo.Product> detailRedis;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RedisTemplate<String, String> stringRedis; // 사용 안 함 (남겨도 무방)

    @Mock
    ValueOperations<String, ProductInfo.Product> detailOps;

    ProductDetailKeyBuilder keyBuilder;
    ProductDetailCache cache;


    private static final Duration TTL = Duration.ofMinutes(2);
    @BeforeEach
    void setUp() {
        keyBuilder = new ProductDetailKeyBuilder();
        cache = new ProductDetailCache(detailRedis, keyBuilder); // 항상 ON, 락 없음
    }

    private ProductInfo.Product dto() { return mock(ProductInfo.Product.class); }

    @Test
    @DisplayName("캐시에 상품상세값이 존재하면 반환한다")
    void hit_returnsCached_withoutSet() {
        Long id = 101L;
        String key = keyBuilder.key(id);

        when(detailRedis.opsForValue()).thenReturn(detailOps);
        when(detailOps.get(key)).thenReturn(dto());

        ProductInfo.Product res = cache.getOrLoad(id, () -> { throw new AssertionError(); });

        assertThat(res).isNotNull();
        verify(detailOps).get(key);
        verify(detailOps, never()).set(anyString(), any(), any());
    }

    @Test
    @DisplayName("캐시에 상품이 존재하지 않으면 로더 실행 후 TTL과 함께 저장한다")
    void miss_runsLoader_setsWithTtl() {
        Long id = 102L;
        String key = keyBuilder.key(id);
        ProductInfo.Product fresh = dto();

        when(detailRedis.opsForValue()).thenReturn(detailOps);
        when(detailOps.get(key)).thenReturn(null);

        ProductInfo.Product res = cache.getOrLoad(id, () -> fresh);

        assertThat(res).isSameAs(fresh);
        verify(detailOps).get(key);
        verify(detailOps).set(key, fresh, TTL);
    }

    @Test
    @DisplayName("evict 호출 시 해당 키를 삭제한다")
    void evict_deletesExactKey() {
        Long id = 103L;
        String key = keyBuilder.key(id);

        cache.evict(id);

        verify(detailRedis).delete(key);
    }
}
