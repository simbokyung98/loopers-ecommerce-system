package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.interfaces.consumer.event.LikeEvent;
import com.loopers.interfaces.consumer.event.LikeEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class CacheInvalidateConsumerIntegrationTest {


    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("중복 이벤트를 발행해도 캐시 무효화는 한 번만 실행된다")
    void duplicateMessageShouldBeHandledOnce() {
        // given
        Long productId = 100L;
        String cacheKey = "v:prod:list:likes";
        redisTemplate.delete(cacheKey);

        String before = redisTemplate.opsForValue().get(cacheKey);
        assertThat(before).isNull(); // 키 없음

        LikeEvent event = new LikeEvent(1L, productId, cacheKey, LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(event);

        // when: 같은 메시지를 두 번 발행
        kafkaTemplate.send("product.like.create.v1", String.valueOf(productId), message);
        kafkaTemplate.send("product.like.create.v1", String.valueOf(productId), message);

        // then: Awaitility로 Redis 상태 검증
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String result = redisTemplate.opsForValue().get(cacheKey);
            assertThat(result).isEqualTo("1"); // 딱 한 번만 증가했어야 함
        });
    }
}
