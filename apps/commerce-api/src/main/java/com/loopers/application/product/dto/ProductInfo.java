package com.loopers.application.product.dto;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductInfo{

    public record Products(
            List<Product> products
    ){}

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        }
    }


    public record Product(
            Long id,
            String name,
            Long stock,
            Long price,
            ProductStatus status,
            Long brandId,
            Long likeCount,
            String brandName
    ) {
        public static Product from(ProductModel productModel, BrandModel brandModel, Long likeCount){
            return new Product(
                    productModel.getId(),
                    productModel.getName(),
                    productModel.getStock(),
                    productModel.getPrice(),
                    productModel.getStatus(),
                    productModel.getBrandId(),
                    likeCount,
                    brandModel.getName()
            );
        }
    }

}
