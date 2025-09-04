package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.interfaces.consumer.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidateConsumer {

    private final EventHandlerService eventHandlerService;
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(
            topics = "${demo-kafka.like.topic-name}",
            groupId = "cache-consumer-group",
            containerFactory = "cacheKafkaListenerContainerFactory"
    )
    public void onLikeEvent(@Payload KafkaMessage<LikeEvent> event,
                            Acknowledgment ack) {
        if (!eventHandlerService.tryConsume(event.eventId(), "cache")) {
            ack.acknowledge();
            return;
        }

        LikeEvent payload = event.payload();
        if (payload.cacheKey() != null) {
            redisTemplate.opsForValue().increment(payload.cacheKey());
            log.info("Cache version bumped for key={} (eventId={})",
                    payload.cacheKey(), event.eventId());
        }
        ack.acknowledge();
    }


}
