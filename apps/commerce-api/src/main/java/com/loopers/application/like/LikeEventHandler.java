package com.loopers.application.like;

import com.loopers.domain.Like.event.LikeCreatedEvent;
import com.loopers.domain.Like.event.LikeDeletedEvent;
import com.loopers.cache.ProductLikeVersionService;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeCreated(LikeCreatedEvent e){
        productService.increaseLikeCount(e.productId());
        likeVersionService.bump();
        log.debug("Like created handled:product={}", e.productId());

        System.out.println("Like created handled: product="+ e.productId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeDeleted(LikeDeletedEvent e){
        productService.decreaseLikeCount(e.productId());
        likeVersionService.bump();
        log.debug("Like deleted handled: product={}", e.productId());
        System.out.println("Like deleted handled: product="+ e.productId());
    }

}
