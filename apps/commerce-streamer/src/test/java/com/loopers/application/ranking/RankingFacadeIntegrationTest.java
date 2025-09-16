package com.loopers.application.ranking;

import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankingFacadeIntegrationTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RankingFacade rankingFacade;

    @Autowired
    private RedisCleanUp redisCleanUp;


    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @Test
    void updateLike_shouldIncrementScore() {
        Long productId = 100L;

        rankingFacade.updateLike(productId, 1);

        String key = "rank:all:" + LocalDate.now();
        Double score = redisTemplate.opsForZSet().score(key, productId.toString());

        assertThat(score).isEqualTo(5.0);
    }

    @Test
    void updateOrder_shouldIncrementByQuantityTimesWeight() {
        Long productId = 200L;

        rankingFacade.updateOrder(productId, 2);

        String key = "rank:all:" + LocalDate.now();
        Double score = redisTemplate.opsForZSet().score(key, productId.toString());

        assertThat(score).isEqualTo(40.0); // 2 * ORDER_SCORE(20)
    }

    @Test
    void multipleEvents_shouldAggregateScoresCorrectly() {
        Long productA = 1L;
        Long productB = 2L;
        Long productC = 3L;

        // A: 좋아요 2번 (2 * 5 = 10점), 주문 1건 수량 1 (1 * 20 = 20점) → 총 30점
        rankingFacade.updateLike(productA, 1);
        rankingFacade.updateLike(productA, 1);
        rankingFacade.updateOrder(productA, 1);

        // B: 조회 3번 (3 * 1 = 3점), 좋아요 1번 (1 * 5 = 5점) → 총 8점
        rankingFacade.updateView(productB, 3);
        rankingFacade.updateLike(productB, 1);

        // C: 주문 2건 수량 2 (2 * 20 = 40점) → 총 40점
        rankingFacade.updateOrder(productC, 2);

        // 랭킹 TOP-3 조회
        List<Long> topProducts = rankingFacade.getTopProducts(3);

        // 점수 검증
        String key = "rank:all:" + LocalDate.now();

        Double scoreA = redisTemplate.opsForZSet().score(key, productA.toString());
        Double scoreB = redisTemplate.opsForZSet().score(key, productB.toString());
        Double scoreC = redisTemplate.opsForZSet().score(key, productC.toString());

        assertThat(scoreA).isEqualTo(30.0);
        assertThat(scoreB).isEqualTo(8.0);
        assertThat(scoreC).isEqualTo(40.0);

        // 순위 검증 (C > A > B)
        assertThat(topProducts).containsExactly(productC, productA, productB);
    }


}
