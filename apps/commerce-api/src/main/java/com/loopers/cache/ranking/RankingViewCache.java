package com.loopers.cache.ranking;

import com.loopers.application.ranking.RankingType;
import com.loopers.application.ranking.RankingViewCriteria;
import com.loopers.application.ranking.RankingViewInfo;
import com.loopers.cache.CachePolicy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RankingViewCache {

    private final RedisTemplate<String, RankingViewInfo.ProductList> rankingRedisTemplate;

    private final RankingCachePolicyRegistry policyRegistry;
    private final RankingViewKeyBuilder keyBuilder;

    public RankingViewCache(
            RedisTemplate<String, RankingViewInfo.ProductList> rankingRedisTemplate,
            RankingCachePolicyRegistry rankingCachePolicyRegistry,
            RankingViewKeyBuilder rankingViewKeyBuilder
    ){

        this.rankingRedisTemplate = rankingRedisTemplate;
        this.policyRegistry = rankingCachePolicyRegistry;
        this.keyBuilder = rankingViewKeyBuilder;

    }

    public RankingViewInfo.ProductList dailyGetOrLoad(
            RankingViewCriteria.SearchTodayRanking criteria,
            Supplier<RankingViewInfo.ProductList> loader
    ) {
        RankingType rankingType = RankingType.일일랭킹;

        // 정책 조회
        CachePolicy cachePolicy = policyRegistry.get(rankingType);

        // 정책상 페이지 범위를 벗어나면 그냥 로딩
        if (criteria.page() > cachePolicy.maxPageInclusive()) {
            return loader.get();
        }

        // 키 생성
        final String key = keyBuilder.build(rankingType, criteria.page(), criteria.size());

        // 캐시 조회
        RankingViewInfo.ProductList cached = rankingRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 남은 TTL 확인
            Long ttl = rankingRedisTemplate.getExpire(key, TimeUnit.SECONDS);

            // refreshAhead 조건 충족 시 비동기 갱신
            if (cachePolicy.shouldRefreshAhead(ttl)) {
                CompletableFuture.runAsync(() -> {
                    RankingViewInfo.ProductList refreshed = loader.get();
                    rankingRedisTemplate.opsForValue().set(key, refreshed, cachePolicy.ttl());
                });
            }
            return cached;
        }

        // 없으면 로드 후 캐시에 저장
        RankingViewInfo.ProductList loaded = loader.get();
        rankingRedisTemplate.opsForValue().set(key, loaded, cachePolicy.ttl());
        return loaded;
    }
}
