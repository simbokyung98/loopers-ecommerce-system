package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.interfaces.consumer.event.LikeEvent;
import com.loopers.interfaces.consumer.event.LikeEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheInvalidateConsumerTest {

    @Mock
    private EventHandlerService eventHandlerService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CacheInvalidateConsumer consumer;

    @Test
    @DisplayName("tryConsume=true 이면 캐시 무효화 없이 ack만 호출된다")
    void onLikeEvent_alreadyConsumed() {
        // given
        LikeEvent payload = new LikeEvent(1L, 100L, "cacheKey", LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(payload);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(eventHandlerService.tryConsume(any(), eq("cache"))).thenReturn(true);

        // when
        consumer.onLikeEvent(message, ack);

        // then
        verify(eventHandlerService).tryConsume(message.eventId(), "cache");
        verify(redisTemplate, never()).opsForValue();
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("tryConsume=false + cacheKey 있으면 increment 호출 후 ack된다")
    void onLikeEvent_shouldIncrementCacheAndAck() {
        // given
        LikeEvent payload = new LikeEvent(1L, 100L, "cacheKey", LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(payload);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(eventHandlerService.tryConsume(any(), eq("cache"))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        consumer.onLikeEvent(message, ack);

        // then
        verify(valueOperations).increment("cacheKey");
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("tryConsume=false + cacheKey 없으면 increment 호출 없이 ack만 된다")
    void onLikeEvent_nullCacheKey() {
        // given
        LikeEvent payload = new LikeEvent(1L, 100L, null, LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(payload);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(eventHandlerService.tryConsume(any(), eq("cache"))).thenReturn(false);

        // when
        consumer.onLikeEvent(message, ack);

        // then
        verify(redisTemplate, never()).opsForValue();
        verify(ack).acknowledge();
    }
}
