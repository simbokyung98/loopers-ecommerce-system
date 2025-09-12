package com.loopers.cache.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** 좋아요 정렬 캐시의 전역 버전(INCR로 무효화) */
@Service
public class ProductLikeVersionService {

    private final RedisTemplate<String, String> stringRedis;

    @Value("${cache.keys.product.like.version}")
    private String likeVersionKey;

    public ProductLikeVersionService(RedisTemplate<String, String> stringRedis) {
        this.stringRedis = stringRedis;
    }

    /** 현재 버전(없으면 1로 초기화) */
    public String current() {
        String v = stringRedis.opsForValue().get(likeVersionKey);
        if (v != null) return v;
        Boolean ok = stringRedis.opsForValue().setIfAbsent(likeVersionKey, "1");
        return Boolean.TRUE.equals(ok) ? "1" : stringRedis.opsForValue().get(likeVersionKey);
    }

    /** 좋아요 이벤트 시 호출 → 즉시 무효화(다음 요청부터 새 키) */
    public void bump() {
        stringRedis.opsForValue().increment(likeVersionKey);
        System.out.println("Like bump"+ likeVersionKey);
    }
}
