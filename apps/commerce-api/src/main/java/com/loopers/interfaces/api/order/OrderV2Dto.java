package com.loopers.interfaces.api.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.purchase.PurchaseCriteria;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderV2Dto {
    public record Order(
            Long orderId,
            Long totalAmount,

            OrderStatus status
    ){
        public static Order from(OrderInfo.OrderResponse order){
            return new Order(order.orderId(), order.finalCount(), order.status());
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
            PaymentType paymentType,
            String cardType,
            String cardNo,
            @NotNull
            List<ProductQuantity> productQuantities
    ){
        public PurchaseCriteria.Purchase toPurchase(Long userId){
            List<OrderCriteria.ProductQuantity> productQuantityList = productQuantities.stream()
                    .map(ProductQuantity::to)
                    .toList();
            return new PurchaseCriteria.Purchase(
                    userId,
                    issueCouponId,
                    address,
                    phoneNumber,
                    name,
                    paymentType,
                    cardType,
                    cardNo,
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
