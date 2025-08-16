package com.loopers.application.product.dto;

import com.loopers.domain.product.ProductCommand;
import com.loopers.interfaces.api.product.OrderType;

public class ProductCriteria {

    public record SearchProducts(
            int page,
            int size,
            OrderType orderType,
            Long brandId
    ){
        public ProductCommand.SearchProducts toCommand(){
            return new ProductCommand.SearchProducts(page, size, orderType, brandId);
        }
    }
}
