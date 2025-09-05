package com.loopers.application.like;

import com.loopers.cache.ProductLikeVersionService;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.Like.event.LikeCreatedEvent;
import com.loopers.domain.Like.event.LikeDeletedEvent;
import com.loopers.domain.Like.event.LikeEvent;
import com.loopers.domain.Like.event.LikeEventType;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventHandler {

    private final ProductService productService;
    private final ProductLikeVersionService likeVersionService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${cache.keys.product.like.version}")
    private String likeVersionKey;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeCreated(LikeCreatedEvent e){
        productService.increaseLikeCount(e.productId());
        log.debug("Like created handled:product={}", e.productId());


        KafkaMessage<LikeEvent> message = KafkaMessage.from(e.withCacheKeysAndType(likeVersionKey, LikeEventType.CREATED));
        kafkaTemplate.send("product.like.create.v1",String.valueOf(e.productId()), message );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeDeleted(LikeDeletedEvent e){
        productService.decreaseLikeCount(e.productId());
        log.debug("Like deleted handled: product={}", e.productId());

        KafkaMessage<LikeEvent> message = KafkaMessage.from(e.withCacheKeysAndType(likeVersionKey, LikeEventType.DELETED));
        kafkaTemplate.send("product.like.create.v1",String.valueOf(e.productId()), message );
    }

}
