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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheInvalidateConsumerTest {

    @Mock
    private EventHandlerService eventHandlerService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CacheInvalidateConsumer consumer;

    @Test
    @DisplayName("LikeEvent 발생 시: 캐시 키 버전 증가")
    void onLikeEvent_shouldInvalidateCache() {
        // given
        LikeEvent payload = new LikeEvent(1L, 100L, "cacheKey", LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(payload);
        Acknowledgment ack = mock(Acknowledgment.class);

        when(eventHandlerService.tryConsume(any(), eq("cache"))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));

        // when
        consumer.onLikeEvent(message, ack);

        // then
        verify(redisTemplate.opsForValue()).increment("cacheKey");
        verify(ack).acknowledge();
    }
}
