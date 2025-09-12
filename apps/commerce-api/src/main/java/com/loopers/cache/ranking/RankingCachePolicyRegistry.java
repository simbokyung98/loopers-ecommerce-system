package com.loopers.cache.ranking;

import com.loopers.application.ranking.RankingType;
import com.loopers.cache.CachePolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;

@Component
public class RankingCachePolicyRegistry {

    private final EnumMap<RankingType, CachePolicy> map = new EnumMap<>(RankingType.class);

    public RankingCachePolicyRegistry(){
        map.put(RankingType.일일랭킹, CachePolicy.of(Duration.ofSeconds(30), 3, Duration.ofSeconds(10), 3));
    }

    public CachePolicy get(RankingType t) { return map.get(t); }
}
