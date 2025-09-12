package com.loopers.application.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RankingWeightFacadeTest {

    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, Object, Object> hashOperations;
    private ApplicationEventPublisher publisher;
    private RankingWeightFacade facade;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        hashOperations = mock(HashOperations.class);
        publisher = mock(ApplicationEventPublisher.class);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        facade = new RankingWeightFacade(redisTemplate, publisher);
    }

    @Test
    void get_shouldReturnDefaultWhenEmpty() {
        // given
        when(hashOperations.entries("rank:all:weights")).thenReturn(Map.of());

        // when
        Weights result = facade.get();

        // then
        assertThat(result.view()).isEqualTo(1);
        assertThat(result.like()).isEqualTo(5);
        assertThat(result.order()).isEqualTo(20);
    }

    @Test
    void get_shouldReturnStoredValues() {
        // given
        when(hashOperations.entries("rank:all:weights"))
                .thenReturn(Map.of("VIEW", "2", "LIKE", "10", "ORDER", "30"));

        // when
        Weights result = facade.get();

        // then
        assertThat(result.view()).isEqualTo(2);
        assertThat(result.like()).isEqualTo(10);
        assertThat(result.order()).isEqualTo(30);
    }

    @Test
    void setWeights_shouldStoreValuesInRedis() {
        // given
        Weights weights = new Weights(2, 10, 30);

        // when
        facade.setWeights(weights);

        // then
        verify(hashOperations).putAll("rank:all:weights",
                Map.of("VIEW", "2", "LIKE", "10", "ORDER", "30"));
    }

    @Test
    void updateWeightsAndCarryOver_shouldPublishEventWhenFactorLessThan1() {
        // given
        Weights weights = new Weights(1, 2, 3);

        // when
        facade.updateWeightsAndCarryOver(weights, 0.8);

        // then
        verify(hashOperations).putAll(any(), any());
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(RankingCarryOverEvent.class);
        RankingCarryOverEvent event = (RankingCarryOverEvent) captor.getValue();
        assertThat(event.date()).isEqualTo(LocalDate.now());
        assertThat(event.factor()).isEqualTo(0.8);
    }

    @Test
    void updateWeightsAndCarryOver_shouldNotPublishEventWhenFactorIsOneOrMore() {
        // given
        Weights weights = new Weights(1, 2, 3);

        // when
        facade.updateWeightsAndCarryOver(weights, 1.0);

        // then
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void carryOver_shouldPublishEvent() {
        // given
        LocalDate date = LocalDate.of(2025, 9, 12);

        // when
        facade.carryOver(date, 0.5);

        // then
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());

        RankingCarryOverEvent event = (RankingCarryOverEvent) captor.getValue();
        assertThat(event.date()).isEqualTo(date);
        assertThat(event.factor()).isEqualTo(0.5);
    }

    @Test
    void carryOver_shouldNotPublishEventWhenFactorIsOneOrMore() {
        // given
        LocalDate date = LocalDate.of(2025, 9, 12);

        // when
        facade.carryOver(date, 1.5);

        // then
        verify(publisher, never()).publishEvent(any());
    }
}
