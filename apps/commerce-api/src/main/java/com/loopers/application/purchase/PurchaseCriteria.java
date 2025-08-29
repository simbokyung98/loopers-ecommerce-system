package com.loopers.application.purchase;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.payment.PaymentType;
import lombok.Builder;

import java.util.List;

public class PurchaseCriteria {

    @Builder
    public record Purchase(
            Long userId,
            Long issueCouponId,
            String address,
            String phoneNumber,
            String name,
            PaymentType paymentType,
            String cardType,
            String cardNo,
            List<OrderCriteria.ProductQuantity> productQuantities
    ){
        public OrderCriteria.Order toOrder(){
            return new OrderCriteria.Order(
                    userId,
                    issueCouponId,
                    address,
                    phoneNumber,
                    name,
                    paymentType,
                    cardType,
                    cardNo,
                    productQuantities
            );
        }

        public PaymentCriteria.CreatePayment toPayment(Long orderId, Long finalCount){
            return new PaymentCriteria.CreatePayment(
                    orderId,
                    userId,
                    paymentType,
                    cardType,
                    cardNo,
                    finalCount
            );
        }
    }
}
