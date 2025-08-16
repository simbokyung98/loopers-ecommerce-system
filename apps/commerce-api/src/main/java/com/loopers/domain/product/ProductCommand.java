package com.loopers.domain.product;

import com.loopers.interfaces.api.product.OrderType;

import java.util.List;

public class ProductCommand {

    public record DeductStocks(
            List<ProductQuantity> productQuantities
    ){

    }

    public record ProductQuantity(
            Long productId,
            Long quantity
    ){
        public static ProductQuantity of(Long productId, Long quantity) {
            return new ProductQuantity(productId, quantity);
        }
    }


    public record SearchProducts(
            int page,
            int size,
            OrderType orderType,
            Long brandId
    ){}

}
