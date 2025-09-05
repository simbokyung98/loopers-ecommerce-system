package com.loopers.application.like;

import com.loopers.domain.Like.event.LikeCreatedEvent;
import com.loopers.domain.Like.event.LikeDeletedEvent;
import com.loopers.cache.ProductLikeVersionService;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest
class LikeEventHandlerIntegrationTest {
    @Autowired
    private ApplicationEventPublisher events;

    @MockitoSpyBean
    private ProductService productService;
    @MockitoSpyBean
    private ProductLikeVersionService likeVersionService;

    private static final Long PRODUCT_ID = 100L;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        // 스파이 호출/스텁 초기화
        Mockito.reset(productService, likeVersionService);
        // 실제 DB를 건드리지 않도록 stub (비동기 핸들러 타이밍 검증이 목적)
        doNothing().when(productService).increaseLikeCount(any());
        doNothing().when(productService).decreaseLikeCount(any());
        doNothing().when(likeVersionService).bump();
    }


    @Nested
    @DisplayName("LikeCreatedEvent 처리")
    class CreatedEvent {


        @Test
        @Transactional
        @DisplayName("롤백되면 핸들러가 실행되지 않는다")
        void createdEvent_rollback_doesNothing() {
            events.publishEvent(new LikeCreatedEvent(USER_ID, PRODUCT_ID));

            // 커밋하지 않고 롤백
            TestTransaction.end();


            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                verify(productService, never()).increaseLikeCount(any());
                verify(likeVersionService, never()).bump();
            });
        }
    }

    @Nested
    @DisplayName("LikeDeletedEvent 처리")
    class DeletedEvent {

        @Test
        @Transactional
        @DisplayName("롤백되면 핸들러가 실행되지 않는다")
        void deletedEvent_rollback_doesNothing() {
            events.publishEvent(new LikeDeletedEvent(USER_ID, PRODUCT_ID));
            TestTransaction.end();

            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                verify(productService, never()).decreaseLikeCount(any());
                verify(likeVersionService, never()).bump();
            });
        }
    }
}
