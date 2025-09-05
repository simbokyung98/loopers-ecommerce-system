package com.loopers.application.order.dto;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public class OrderInfo {

    public record OrderResponse(
            Long orderId,
            Long finalAmount,
            OrderStatus status

    ){
        public static OrderResponse from(OrderModel orderModel){
            return new OrderResponse(orderModel.getId(), orderModel.getFinalAmount(), orderModel.getStatus());
        }

        public static OrderResponse from(OrderResult.Order order){
            return new OrderResponse(order.id(), order.finalAmount(), order.status());
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

    public record OrderDetail(
            Long id,
            Long userId,
            Long issueCouponId,
            Long totalAmount,
            Long finalAmount,
            String address,
            String phoneNumber,
            String name,
            String status, // 보통 응답에서는 Enum 대신 String
            List<OrderItemResponse> orderItems
    ){
        public static OrderDetail from(OrderResult.Order order) {
            List<OrderItemResponse> orderItems = order.orderItems().stream()
                    .map(OrderItemResponse::from)
                    .toList();

            return new OrderDetail(
                    order.id(),
                    order.userId(),
                    order.issueCouponId(),
                    order.totalAmount(),
                    order.finalAmount(),
                    order.address(),
                    order.phoneNumber(),
                    order.name(),
                    order.status().name(),   // Enum → String 변환
                    orderItems
            );
        }
    }

    public record OrderItemResponse(
            Long id,
            Long orderId,
            Long productId,
            String name,
            Long price,
            Long quantity,
            String status
    ) {
        public static OrderItemResponse from(OrderResult.OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.id(),
                    orderItem.orderId(),
                    orderItem.productId(),
                    orderItem.name(),
                    orderItem.price(),
                    orderItem.quantity(),
                    orderItem.status().name() // Enum → String
            );
        }
    }
}
