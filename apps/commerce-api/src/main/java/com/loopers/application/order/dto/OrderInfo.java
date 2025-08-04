package com.loopers.application.order.dto;

import com.loopers.domain.order.OrderModel;

public class OrderInfo {

    public record Order(
            Long orderId,
            Long totalAmount
    ){
        public static Order from(OrderModel orderModel){
            return new Order(orderModel.getId(), orderModel.getAmount());
        }
    }
}
