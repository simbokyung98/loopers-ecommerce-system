package com.loopers.application.product.dto;

import com.loopers.interfaces.api.product.OrderType;

public class ProductCriteria {

    public record SearchProducts(
            int page,
            int size,
            OrderType orderType
    ){}
}
