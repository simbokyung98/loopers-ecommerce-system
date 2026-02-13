package com.loopers.application.ranking;


public record RankValue(
        Long productId,
        Long likeCount,
        Long saleCount,
        Long viewCount
){

}
