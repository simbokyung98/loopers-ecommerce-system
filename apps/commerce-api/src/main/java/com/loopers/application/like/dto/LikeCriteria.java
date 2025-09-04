package com.loopers.application.like.dto;


import com.loopers.domain.Like.event.LikeCreatedEvent;
import com.loopers.domain.Like.event.LikeDeletedEvent;

public class LikeCriteria {
    public record Like(
            Long userId,
            Long productId
    ) {
        public static Like of(Long userId, Long productId) {
            return new Like(userId, productId);
        }

        public LikeCreatedEvent ofCreatedEvent(){
            return new LikeCreatedEvent(productId, userId);
        }
    }

    public record Dislike(
            Long userId,
            Long productId
    ) {
        public static Dislike of(Long userId, Long productId) {
            return new Dislike(userId, productId);
        }

        public LikeDeletedEvent ofDeleteEvent(){
            return new LikeDeletedEvent(productId, userId);
        }
    }

}
