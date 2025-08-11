package com.loopers.interfaces.api.like;

import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.like.dto.LikeInfo;
import com.loopers.domain.product.ProductStatus;

import java.util.List;

public class LikeV1Dto {
    public record LikeRequest(
            Long productId
    ){
        public LikeCriteria.Like toCriteria(Long userId){
            return new LikeCriteria.Like(userId, productId);
        }
    }


    public record DislikeRequest(

            Long productId
    ){
        public LikeCriteria.Dislike toCriteria(Long userId){
            return new LikeCriteria.Dislike(userId, productId);
        }
    }

    public record LikeProductsResponse(
            Long userId,
            List<LikeProductResponse> likeProducts
    ){
        public static LikeProductsResponse of(LikeInfo.LikeProducts likeProducts) {
            List<LikeProductResponse> items = likeProducts.likeProducts().stream()
                    .map(LikeProductResponse::from)
                    .toList();
            return new LikeProductsResponse(likeProducts.userId(), items);
        }

    }

    public record LikeProductResponse(
            Long id,
            String name,
            Long stock,
            Long price,
            ProductStatus status,
            Long brandId,
            Long likeCount
    ) {
        public static LikeProductResponse from(LikeInfo.LikeProduct likeProduct){
            return new LikeProductResponse(
                    likeProduct.id(),
                    likeProduct.name(),
                    likeProduct.stock(),
                    likeProduct.price(),
                    likeProduct.status(),
                    likeProduct.brandId(),
                    likeProduct.likeCount()
            );
        }
    }
}
