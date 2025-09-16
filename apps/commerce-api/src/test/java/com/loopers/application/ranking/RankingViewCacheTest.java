package com.loopers.application.ranking;

import com.loopers.cache.CachePolicy;
import com.loopers.cache.ranking.RankingCachePolicyRegistry;
import com.loopers.cache.ranking.RankingViewCache;
import com.loopers.cache.ranking.RankingViewKeyBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RankingViewCacheTest {

    @Mock
    private RedisTemplate<String, RankingViewInfo.ProductList> redisTemplate;
    @Mock
    private ValueOperations<String, RankingViewInfo.ProductList> valueOps;
    @Mock
    private RankingCachePolicyRegistry policyRegistry;
    @Mock
    private RankingViewKeyBuilder keyBuilder;

    @InjectMocks
    private RankingViewCache rankingViewCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private RankingViewInfo.ProductList dummyList() {
        RankingViewInfo.Product product = new RankingViewInfo.Product(
                1,
                100L,
                "테스트상품",
                10L,
                5000L,
                null,     // ProductStatus 생략 가능 (mock 상황)
                200L,
                5L,
                "브랜드A"
        );
        return new RankingViewInfo.ProductList(
                0,
                20,
                LocalDate.now(),
                List.of(product)
        );
    }

    @Test
    void 캐시미스일때_loader호출하고저장한다() {
        RankingViewCriteria.SearchTodayRanking criteria = new RankingViewCriteria.SearchTodayRanking(0, 20, LocalDate.now());
        CachePolicy policy = CachePolicy.of(Duration.ofMinutes(1), 10);

        when(policyRegistry.get(RankingType.일일랭킹)).thenReturn(policy);
        when(keyBuilder.build(RankingType.일일랭킹, criteria.page(), criteria.size()))
                .thenReturn("rank:daily:0:20");

        when(valueOps.get("rank:daily:0:20")).thenReturn(null);

        RankingViewInfo.ProductList expected = dummyList();
        Supplier<RankingViewInfo.ProductList> loader = () -> expected;

        RankingViewInfo.ProductList result = rankingViewCache.dailyGetOrLoad(criteria, loader);

        assertThat(result).isSameAs(expected);
        verify(valueOps).set("rank:daily:0:20", expected, policy.ttl());
    }

    @Test
    void 캐시히트일때_loader호출하지않는다() {
        RankingViewCriteria.SearchTodayRanking criteria = new RankingViewCriteria.SearchTodayRanking(0, 20, LocalDate.now());
        CachePolicy policy = CachePolicy.of(Duration.ofMinutes(1), 10);

        when(policyRegistry.get(RankingType.일일랭킹)).thenReturn(policy);
        when(keyBuilder.build(RankingType.일일랭킹, criteria.page(), criteria.size()))
                .thenReturn("rank:daily:0:20");

        RankingViewInfo.ProductList cached = dummyList();
        when(valueOps.get("rank:daily:0:20")).thenReturn(cached);

        @SuppressWarnings("unchecked")
        Supplier<RankingViewInfo.ProductList> loader = mock(Supplier.class);

        RankingViewInfo.ProductList result = rankingViewCache.dailyGetOrLoad(criteria, loader);

        assertThat(result).isSameAs(cached);
        verify(loader, never()).get();
    }

    @Test
    void refreshAhead조건이면_비동기갱신트리거된다() {
        RankingViewCriteria.SearchTodayRanking criteria = new RankingViewCriteria.SearchTodayRanking(0, 20, LocalDate.now());
        CachePolicy policy = CachePolicy.of(Duration.ofMinutes(1), 10, Duration.ofSeconds(30), 0);

        when(policyRegistry.get(RankingType.일일랭킹)).thenReturn(policy);
        when(keyBuilder.build(RankingType.일일랭킹, criteria.page(), criteria.size()))
                .thenReturn("rank:daily:0:20");

        RankingViewInfo.ProductList cached = dummyList();
        when(valueOps.get("rank:daily:0:20")).thenReturn(cached);
        when(redisTemplate.getExpire("rank:daily:0:20", TimeUnit.SECONDS)).thenReturn(20L); // TTL 20초 남음

        Supplier<RankingViewInfo.ProductList> loader = this::dummyList;

        RankingViewInfo.ProductList result = rankingViewCache.dailyGetOrLoad(criteria, loader);

        assertThat(result).isSameAs(cached);

        verify(valueOps, timeout(1000).atLeast(1))
                .set(eq("rank:daily:0:20"), any(RankingViewInfo.ProductList.class), eq(policy.ttl()));
    }
}
