package com.loopers.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** 좋아요 정렬 캐시의 전역 버전(INCR로 무효화) */
@Service
public class ProductLikeVersionService {

    private final RedisTemplate<String, String> stringRedis;
    private static final String KEY = "v:prod:list:likes";
    public ProductLikeVersionService(RedisTemplate<String, String> stringRedis) {
        this.stringRedis = stringRedis;
    }

    /** 현재 버전(없으면 1로 초기화) */
    public String current() {
        String v = stringRedis.opsForValue().get(KEY);
        if (v != null) return v;
        Boolean ok = stringRedis.opsForValue().setIfAbsent(KEY, "1");
        return Boolean.TRUE.equals(ok) ? "1" : stringRedis.opsForValue().get(KEY);
    }

    /** 좋아요 이벤트 시 호출 → 즉시 무효화(다음 요청부터 새 키) */
    public void bump() {
        stringRedis.opsForValue().increment(KEY);
    }
}
