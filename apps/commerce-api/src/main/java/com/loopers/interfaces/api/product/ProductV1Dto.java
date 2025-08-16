package com.loopers.interfaces.api.product;

import com.loopers.application.common.PageInfo;
import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.product.ProductStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.function.Function;

public class ProductV1Dto {

    public record SearchProductRequest(
            @NotNull
            int page,
            @NotNull
            int size,
            @NotNull
            String orderTypeRequest,
            Long brandId
    ){
        public ProductCriteria.SearchProducts toCriteria(){
            OrderType orderType = OrderType.fromValue(orderTypeRequest);
            return new ProductCriteria.SearchProducts(page, size, orderType, brandId);
        }
    }

    public record ProductResponse(
            Long id,
            String name,
            Long stock,
            Long price,
            ProductStatus status,
            Long brandId,
            Long likeCount,
            String brandName
    ){
        public static ProductResponse from(ProductInfo.Product product){
            return new ProductResponse(
                    product.id(),
                    product.name(),
                    product.stock(),
                    product.price(),
                    product.status(),
                    product.brandId(),
                    product.likeCount(),
                    product.brandName()
            );
        }
    }

    public record PageMeta(int page, int size, long totalElements, int totalPages) {
        public static PageMeta from(PageInfo.PageMeta m) {
            return new PageMeta(m.page(), m.size(), m.totalElements(), m.totalPages());
        }
    }

    public record PageEnvelope<T>(List<T> items, PageMeta meta) {
        public static <S, T> PageEnvelope<T> from(
                PageInfo.PageEnvelope<S> src,
                Function<S, T> mapper
        ) {
            return new PageEnvelope<>(
                    src.content().stream().map(mapper).toList(),
                    PageMeta.from(src.meta())
            );
        }
    }
}

