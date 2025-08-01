package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record PlaceOrder(
            Long userId,
            Long amount,
            String address,
            String phoneNumber,
            String name,
            OrderStatus status,
            List<Product> products
    ){}

    public record Product(
            Long productId,
            String name,
            Long price,
            Long quantity
    ){}


}
