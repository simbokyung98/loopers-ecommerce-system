package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {

    public record PlaceOrder(
            Long userId,
            Long couponId,
            Long totalAmount,
            Long finalAmount,
            String address,
            String phoneNumber,
            String name,
            List<Product> products
    ){
    }

    public record Product(
            Long productId,
            String name,
            Long price,
            Long quantity
    ){}


}
