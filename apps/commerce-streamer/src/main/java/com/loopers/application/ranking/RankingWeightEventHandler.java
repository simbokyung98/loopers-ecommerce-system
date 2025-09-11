package com.loopers.application.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RankingWeightEventHandler {

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    @EventListener
    public void handleCarryOver(RankingCarryOverEvent event) {
        double factor = event.factor();
        String key = "rank:all:" + event.date();

        Set<ZSetOperations.TypedTuple<String>> rows =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        if (rows == null || rows.isEmpty()) return;

        Set<ZSetOperations.TypedTuple<String>> discounted = rows.stream()
                .map(t -> new DefaultTypedTuple<>(
                        t.getValue(),
                        (t.getScore() == null ? 0.0 : t.getScore() * factor)
                ))
                .collect(Collectors.toSet());

        redisTemplate.opsForZSet().add(key, discounted);
        redisTemplate.expire(key, Duration.ofDays(30));
    }
}
