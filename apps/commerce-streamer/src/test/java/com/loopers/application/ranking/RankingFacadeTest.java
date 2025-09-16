package com.loopers.application.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RankingFacadeTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RankingFacade rankingFacade;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void updateLike_incrementsScoreWithCorrectWeight() {
        rankingFacade.updateLike(123L, 1);

        String todayKey = "rank:all:" + LocalDate.now();
        verify(zSetOperations).incrementScore(todayKey, "123", 5.0);
    }

    @Test
    void updateOrder_incrementsScoreWithQuantityTimesWeight() {
        rankingFacade.updateOrder(123L, 2);

        String todayKey = "rank:all:" + LocalDate.now();
        verify(zSetOperations).incrementScore(todayKey, "123", 40.0);
    }

}
