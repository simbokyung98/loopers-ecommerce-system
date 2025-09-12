package com.loopers.application.ranking;


import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class RankingViewInfo {


    public record ProductList(
            int page,
            int size,
            LocalDate date,
            List<Product> products
    ) implements Serializable {}
    public record Product(
            int rank,
            Long id,
            String name,
            Long stock,
            Long price,
            ProductStatus status,
            Long brandId,
            Long likeCount,
            String brandName
    )  implements Serializable {
        public static RankingViewInfo.Product from(ProductModel productModel, BrandModel brandModel, int rank){
            return new RankingViewInfo.Product(
                    rank,
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
}
