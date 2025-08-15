package com.loopers.application.product;


import com.loopers.application.common.PageInfo;
import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.cache.ProductCachePolicyRegistry;
import com.loopers.cache.ProductLikeVersionService;
import com.loopers.cache.ProductListCache;
import com.loopers.cache.ProductListKeyBuilder;
import com.loopers.interfaces.api.product.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("상품 목록 캐시 (락 제거 + 좋아요순 버전 키)")
@ExtendWith(MockitoExtension.class)
class ProductListCacheTest {

    @Mock
    RedisTemplate<String, PageInfo.PageEnvelope<ProductInfo.Product>> pageRedis;

    @Mock
    ValueOperations<String, PageInfo.PageEnvelope<ProductInfo.Product>> pageOps;

    @Mock
    ProductLikeVersionService likeVersionService;

    ProductCachePolicyRegistry policies;
    ProductListKeyBuilder keyBuilder;
    ProductListCache cache;

    @BeforeEach
    void setUp() {

        policies = new ProductCachePolicyRegistry();
        keyBuilder = new ProductListKeyBuilder();

        cache = new ProductListCache(pageRedis, policies, keyBuilder, likeVersionService);
    }

    private PageInfo.PageEnvelope<ProductInfo.Product> envelope() {
        PageInfo.PageMeta meta = new PageInfo.PageMeta(0, 20, 0L, 0);
        return PageInfo.PageEnvelope.of(Collections.emptyList(), meta);
    }

    @Nested
    @DisplayName("최신순")
    class LatestOrder {

        @Test
        @DisplayName("미스이면 로더 결과를 TTL과 함께 저장한다 (45초)")
        void latest_miss_setsWithTtl_andReturns() {
            ProductCriteria.SearchProducts criteria =
                    new ProductCriteria.SearchProducts(0, 20, OrderType.최신순, null);

            when(pageRedis.opsForValue()).thenReturn(pageOps);
            String key = keyBuilder.build(OrderType.최신순, null, 0, 20);
            when(pageOps.get(key)).thenReturn(null);

            Supplier<PageInfo.PageEnvelope<ProductInfo.Product>> loader = ProductListCacheTest.this::envelope;

            PageInfo.PageEnvelope<ProductInfo.Product> res = cache.getOrLoad(criteria, loader);

            assertThat(res).isNotNull();
            verify(pageOps).set(key, res, Duration.ofSeconds(45));
        }

        @Test
        @DisplayName("히트이면 로더 호출 없이 캐시를 반환한다")
        void latest_hit_returnsCached_withoutLoader() {
            ProductCriteria.SearchProducts criteria =
                    new ProductCriteria.SearchProducts(0, 20, OrderType.최신순, null);

            when(pageRedis.opsForValue()).thenReturn(pageOps);
            String key = keyBuilder.build(OrderType.최신순, null, 0, 20);
            PageInfo.PageEnvelope<ProductInfo.Product> cached = envelope();
            when(pageOps.get(key)).thenReturn(cached);

            @SuppressWarnings("unchecked")
            Supplier<PageInfo.PageEnvelope<ProductInfo.Product>> loader = mock(Supplier.class);

            PageInfo.PageEnvelope<ProductInfo.Product> res = cache.getOrLoad(criteria, loader);

            assertThat(res).isSameAs(cached);
            verify(loader, never()).get();
            verify(pageOps, never()).set(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("높은좋아요순(메인)")
    class TopLikesOrder {

        @Test
        @DisplayName("미스이면 '버전 키'로 저장한다 (30초)")
        void topLikes_miss_usesVersionedKey() {
            ProductCriteria.SearchProducts criteria =
                    new ProductCriteria.SearchProducts(0, 20, OrderType.높은좋아요순, null);

            when(pageRedis.opsForValue()).thenReturn(pageOps);
            when(likeVersionService.current()).thenReturn("1");

            String key = String.format("prod:list:%s:%s:g%s:p%d:s%d",
                    OrderType.높은좋아요순.name(), "all", "1", 0, 20);
            when(pageOps.get(key)).thenReturn(null);

            PageInfo.PageEnvelope<ProductInfo.Product> fresh = envelope();
            PageInfo.PageEnvelope<ProductInfo.Product> res =
                    cache.getOrLoad(criteria, () -> fresh);

            assertThat(res).isSameAs(fresh);
            verify(pageOps).set(key, fresh, Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("정책 우회")
    class PolicyBypass {

        @Test
        @DisplayName("maxPageInclusive 초과 페이지는 캐시를 사용하지 않는다")
        void overMaxPage_bypassesCache_entirely() {
            // 최신순 maxPageInclusive=3 → page=5면 캐시 우회
            ProductCriteria.SearchProducts criteria =
                    new ProductCriteria.SearchProducts(5, 20, OrderType.최신순, null);

            @SuppressWarnings("unchecked")
            Supplier<PageInfo.PageEnvelope<ProductInfo.Product>> loader = mock(Supplier.class);
            when(loader.get()).thenReturn(envelope());

            PageInfo.PageEnvelope<ProductInfo.Product> res = cache.getOrLoad(criteria, loader);

            assertThat(res).isNotNull();
            verify(loader).get();
            verify(pageOps, never()).get(anyString());
            verify(pageOps, never()).set(anyString(), any(), any());
        }
    }
}
