package com.loopers.application.product.dto;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;

public record ProductInfo(
        Long id,
        String name,
        Long stock,
        Long price,
        ProductStatus status,
        Long brandId,
        Long likeCount,
        String brandName
) {
    public static ProductInfo from(ProductModel productModel, BrandModel brandModel){
        return new ProductInfo(
                productModel.getId(),
                productModel.getName(),
                productModel.getStock(),
                productModel.getPrice(),
                productModel.getStatus(),
                productModel.getBrandId(),
                productModel.getLikeCount(),
                brandModel.getName()
        );
    }
}
