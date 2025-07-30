package com.loopers.application.like.dto;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;

public record LikeProductInfo(
        Long id,
        String name,
        Long stock,
        Long price,
        ProductStatus status,
        Long brandId,
       Long likeCount
) {
    public static LikeProductInfo from(ProductModel productModel){
        return new LikeProductInfo(
                productModel.getId(),
                productModel.getName(),
                productModel.getStock(),
                productModel.getPrice(),
                productModel.getStatus(),
                productModel.getBrandId(),
                productModel.getLikeCount()
        );
    }
}
