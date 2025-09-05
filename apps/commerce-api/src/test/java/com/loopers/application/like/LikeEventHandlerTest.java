package com.loopers.application.like;

import com.loopers.cache.ProductLikeVersionService;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.Like.event.LikeCreatedEvent;
import com.loopers.domain.Like.event.LikeDeletedEvent;
import com.loopers.domain.Like.event.LikeEvent;
import com.loopers.domain.Like.event.LikeEventType;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LikeEventHandlerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductLikeVersionService likeVersionService;

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @InjectMocks
    private LikeEventHandler handler;

    @Test
    @DisplayName("좋아요 생성 이벤트 발생 시: 상품 좋아요 수 증가 + Kafka 메시지 발행")
    void onLikeCreated_shouldIncreaseLikeCount_andSendKafkaMessage() {
        // given
        LikeCreatedEvent event = new LikeCreatedEvent(1L, 100L);

        // when
        handler.onLikeCreated(event);

        // then
        verify(productService).increaseLikeCount(100L);
        verify(kafkaTemplate).send(
                eq("product.like.create.v1"),
                eq("100"), // key = productId string
                argThat(message -> {
                    if (!(message instanceof KafkaMessage<?> kafkaMessage)) return false;
                    if (!(kafkaMessage.payload() instanceof LikeEvent likeEvent)) return false;
                    return likeEvent.productId().equals(100L) && likeEvent.type() == LikeEventType.CREATED;
                })
        );
    }

    @Test
    @DisplayName("좋아요 취소 이벤트 발생 시: 상품 좋아요 수 감소 + Kafka 메시지 발행")
    void onLikeDeleted_shouldDecreaseLikeCount_andSendKafkaMessage() {
        // given
        LikeDeletedEvent event = new LikeDeletedEvent(1L, 100L);

        // when
        handler.onLikeDeleted(event);

        // then
        verify(productService).decreaseLikeCount(100L);
        verify(kafkaTemplate).send(
                eq("product.like.create.v1"),
                eq("100"),
                argThat(message -> {
                    if (!(message instanceof KafkaMessage<?> kafkaMessage)) return false;
                    if (!(kafkaMessage.payload() instanceof LikeEvent likeEvent)) return false;
                    return likeEvent.productId().equals(100L) && likeEvent.type() == LikeEventType.DELETED;
                })
        );
    }
}
