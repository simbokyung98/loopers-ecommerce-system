package com.loopers.application.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RankingFacade {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int LIKE_WEIGHT = 5;
    private static final int ORDER_WEIGHT = 20;
    private static final int VIEW_WEIGHT = 1;

    public void updateLike(Long productId, int delta) {
        updateScore(productId, delta * LIKE_WEIGHT);
    }

    public void updateOrder(Long productId, int delta) {
        updateScore(productId, delta * ORDER_WEIGHT);
    }

    public void updateView(Long productId, int delta) {
        updateScore(productId, delta * VIEW_WEIGHT);
    }

    private void updateScore(Long productId, int score) {
        String key = todayKey();
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
        redisTemplate.expire(key, Duration.ofDays(30));
    }

    public List<Long> getTopProducts(int topN) {
        String key = todayKey();
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(key, 0, topN - 1);
        return ids == null ? List.of() : ids.stream().map(Long::valueOf).toList();
    }

    private String todayKey() {
        return "rank:all:" + LocalDate.now();
    }

}
