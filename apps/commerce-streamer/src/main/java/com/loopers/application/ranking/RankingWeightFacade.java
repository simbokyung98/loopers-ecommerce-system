package com.loopers.application.ranking;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingWeightFacade {

    private final RedisTemplate<String, String> redisTemplate;
    private final ApplicationEventPublisher events;

    private static final String WEIGHT_KEY = "rank:all:weights"; // HASH: VIEW/LIKE/ORDER

    /** 현재 weight 읽기 (없으면 DEFAULT만 리턴) */
    public Weights get() {
        Map<Object, Object> m = redisTemplate.opsForHash().entries(WEIGHT_KEY);
        if (m == null || m.isEmpty()) return Weights.DEFAULT;
        return new Weights(
                parse(m.get("VIEW"), 1),
                parse(m.get("LIKE"), 5),
                parse(m.get("ORDER"), 20)
        );
    }

    public void setWeights(Weights w) {
        redisTemplate.opsForHash().putAll(WEIGHT_KEY, Map.of(
                "VIEW",  String.valueOf(w.view()),
                "LIKE",  String.valueOf(w.like()),
                "ORDER", String.valueOf(w.order())
        ));
    }

    // 4) 편의: 한 번에 변경+감가
    public void updateWeightsAndCarryOver(Weights newW, double factor) {
        setWeights(newW);

        if (factor >= 1.0d) return;
        events.publishEvent(new RankingCarryOverEvent(LocalDate.now(),factor));
    }


    private int parse(Object o, int def) {
        try { return o == null ? def : Integer.parseInt(o.toString()); }
        catch (Exception e) { return def; }
    }

    public void carryOver(LocalDate date, double factor) {
        double f = Math.max(0.0d, Math.min(factor, 1.0d));
        if (f >= 1.0d) return;
        events.publishEvent(new RankingCarryOverEvent(date, f));
    }
}
