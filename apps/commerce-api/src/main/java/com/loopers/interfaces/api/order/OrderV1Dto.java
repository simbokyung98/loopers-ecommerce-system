package com.loopers.interfaces.api.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.purchase.PurchaseCriteria;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderV1Dto {
    public record Order(
            Long orderId,
            Long totalAmount,

            OrderStatus status
    ){
        public static OrderV1Dto.Order from(OrderInfo.OrderResponse order){
            return new OrderV1Dto.Order(order.orderId(), order.finalCount(), order.status());
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
        public OrderCriteria.Order toOrder(Long userId){
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

        public PurchaseCriteria.Purchase toPurchase(Long userId){
            List<OrderCriteria.ProductQuantity> productQuantityList = productQuantities.stream()
                    .map(ProductQuantity::to)
                    .toList();

            return PurchaseCriteria.Purchase.builder()
                    .userId(userId)
                    .issueCouponId(issueCouponId)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .name(name)
                    .paymentType(PaymentType.POINT)
                    .productQuantities(productQuantityList)
                    .build();
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
