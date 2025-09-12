package com.loopers.application.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RankingWeightEventHandlerTest {

    private RedisTemplate<String, String> redisTemplate;
    private ZSetOperations<String, String> zSetOperations;
    private RankingWeightEventHandler handler;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        zSetOperations = mock(ZSetOperations.class);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        handler = new RankingWeightEventHandler(redisTemplate);
    }

    @Test
    void handleCarryOver_shouldApplyFactorToScores() {
        // given
        LocalDate date = LocalDate.of(2025, 9, 12);
        String key = "rank:all:" + date;
        RankingCarryOverEvent event = new RankingCarryOverEvent(date, 0.5);

        Set<ZSetOperations.TypedTuple<String>> rows = Set.of(
                new DefaultTypedTuple<>("101", 100.0),
                new DefaultTypedTuple<>("102", 200.0)
        );
        when(zSetOperations.rangeWithScores(key, 0, -1)).thenReturn(rows);

        // when
        handler.handleCarryOver(event);

        // then
        ArgumentCaptor<Set<ZSetOperations.TypedTuple<String>>> captor = ArgumentCaptor.forClass(Set.class);
        verify(zSetOperations).add(eq(key), captor.capture());
        verify(redisTemplate).expire(eq(key), any());

        Set<ZSetOperations.TypedTuple<String>> discounted = captor.getValue();
        assertThat(discounted)
                .extracting(ZSetOperations.TypedTuple::getValue)
                .containsExactlyInAnyOrder("101", "102");

        assertThat(discounted)
                .extracting(ZSetOperations.TypedTuple::getScore)
                .containsExactlyInAnyOrder(50.0, 100.0); // 0.5배 된 값
    }

    @Test
    void handleCarryOver_shouldDoNothingWhenNoRows() {
        // given
        LocalDate date = LocalDate.of(2025, 9, 12);
        String key = "rank:all:" + date;
        RankingCarryOverEvent event = new RankingCarryOverEvent(date, 0.8);

        when(zSetOperations.rangeWithScores(key, 0, -1)).thenReturn(Set.of());

        // when
        handler.handleCarryOver(event);

        // then
        verify(zSetOperations, never()).add(anyString(), any());
        verify(redisTemplate, never()).expire(anyString(), any());
    }
}
