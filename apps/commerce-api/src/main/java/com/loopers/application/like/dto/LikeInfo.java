package com.loopers.application.like.dto;

import com.loopers.domain.Like.LikeModel;

public record LikeInfo(
        Long id,
        Long userId,
        Long productId
) {
    public static LikeInfo from(LikeModel likeModel){
        return new LikeInfo(
                likeModel.getId(),
                likeModel.getUserId(),
                likeModel.getProductId()
        );
    }
}
