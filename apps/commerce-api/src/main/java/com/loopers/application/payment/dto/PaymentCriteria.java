package com.loopers.application.payment.dto;


import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PaymentType;
import com.loopers.infrastructure.http.dto.PaymentCreateRequest;

public class PaymentCriteria {
    public record CreatePayment(
            Long orderId,
            Long userId,
            PaymentType type,
            String cardType,
            String cardNo,
            Long amount
    ){

        public PaymentCommand.CreateCardPayment toCommand(String transactionKey){
            return new PaymentCommand.CreateCardPayment(
                    orderId,
                    userId,
                    type,
                    amount,
                    transactionKey
            );
        }
        public PaymentCommand.CreatePointPayment toPointCommand(PaymentStatus status){
            return new PaymentCommand.CreatePointPayment(
                    orderId,
                    userId,
                    type,
                    amount,
                    status
            );
        }

        public PaymentCreateRequest toRequest(){
            return new PaymentCreateRequest(
                    toPgOrderId(orderId),
                    cardType,
                    cardNo,
                    String.valueOf(amount)
            );
        }


        private static String toPgOrderId(Long orderId) {
            return "ORD-" + String.format("%06d", orderId);
        }


    }
}
