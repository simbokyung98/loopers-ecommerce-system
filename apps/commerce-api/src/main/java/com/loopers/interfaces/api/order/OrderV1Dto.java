package com.loopers.interfaces.api.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderV1Dto {
    public record Order(
            Long orderId,
            Long totalAmount
    ){
        public static Order from(OrderInfo.OrderResponse order){
            return new Order(order.orderId(), order.totalAmount());
        }
    }

    public record OrderRequest(

            Long issueCouponId,
            @NotNull
            String address,
            @NotNull
            String phoneNumber,
            @NotNull
            String name,
            @NotNull
            List<ProductQuantity> productQuantities
    ){
        public OrderCriteria.Order to(Long userId){
            List<OrderCriteria.ProductQuantity> productQuantityList = productQuantities.stream()
                    .map(ProductQuantity::to)
                    .toList();
            return new OrderCriteria.Order(
                    userId,
                    issueCouponId,
                    address,
                    phoneNumber,
                    name,
                    productQuantityList
            );
        }
    }

    public record ProductQuantity(
            @NotNull
            Long productId,
            @NotNull
            Long quantity
    ){
        public OrderCriteria.ProductQuantity to(){
            return new OrderCriteria.ProductQuantity(productId, quantity);
        }
    }

    public record UserOrdersResponse(
            List<OrderResponse> orderResponses
    ){
        public static UserOrdersResponse from(OrderInfo.UserOrders userOrders){
            List<OrderResponse> responses = userOrders.orders()
                    .stream().map(OrderResponse::from)
                    .toList();
            return new UserOrdersResponse(responses);
        }
    }

    public record OrderResponse(
            Long id,
            Long userId,
            String address,
            String phoneNumber,
            String name
    ){
        public static OrderResponse from(OrderInfo.Order order){
            return new OrderResponse(
                    order.id(),
                    order.userId(),
                    order.address(),
                    order.phoneNumber(),
                    order.name()
            );
        }
    }

}
