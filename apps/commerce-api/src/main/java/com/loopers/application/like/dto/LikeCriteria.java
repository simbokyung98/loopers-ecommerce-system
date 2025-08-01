package com.loopers.application.like.dto;


public class LikeCriteria {
    public record Like(
            Long userId,
            Long productId
    ) {
        public static Like of(Long userId, Long productId) {
            return new Like(userId, productId);
        }
    }

    public record Dislike(
            Long userId,
            Long productId
    ) {
        public static Dislike of(Long userId, Long productId) {
            return new Dislike(userId, productId);
        }
    }

}
