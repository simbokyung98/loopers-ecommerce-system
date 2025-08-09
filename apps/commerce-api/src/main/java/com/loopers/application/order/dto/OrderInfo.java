package com.loopers.application.order.dto;

import com.loopers.domain.order.OrderModel;

import java.util.List;

public class OrderInfo {

    public record OrderResponse(
            Long orderId,
            Long totalAmount
    ){
        public static OrderResponse from(OrderModel orderModel){
            return new OrderResponse(orderModel.getId(), orderModel.getAmount());
        }
    }

    public record Order(
            Long id,
            Long userId,
            String address,
            String phoneNumber,
            String name
    ){
        public static Order from(OrderModel orderModel){
            return new Order(
                    orderModel.getId(),
                    orderModel.getUserId(),
                    orderModel.getAddress(),
                    orderModel.getPhoneNumber(),
                    orderModel.getName()
            );
        }
    }

    public record UserOrders(
             List<Order> orders
    ){

        public static UserOrders from(List<OrderModel> models){
            List<Order> orderList = models.stream()
                    .map(Order::from)
                    .toList();

            return new UserOrders(orderList);
        }

    }
}
