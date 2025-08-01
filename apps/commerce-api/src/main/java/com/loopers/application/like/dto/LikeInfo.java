package com.loopers.application.like.dto;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;

import java.util.List;

public class LikeInfo{

    public record LikeProducts(
            Long userId,
            List<LikeProduct> likeProducts
    ){
        public static LikeProducts of(Long userId, List<ProductModel> models) {
            List<LikeProduct> items = models.stream()
                    .map(LikeProduct::from)
                    .toList();
            return new LikeProducts(userId, items);
        }

    }

    public record LikeProduct(
            Long id,
            String name,
            Long stock,
            Long price,
            ProductStatus status,
            Long brandId,
            Long likeCount
    ) {
        public static LikeProduct from(ProductModel productModel){
            return new LikeProduct(
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


}

