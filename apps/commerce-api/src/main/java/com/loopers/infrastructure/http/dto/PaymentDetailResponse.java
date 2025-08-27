package com.loopers.infrastructure.http.dto;


import com.loopers.application.payment.dto.PaymentGatewayResult;

public record PaymentDetailResponse(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        String status,
        String reason

) {

    public PaymentGatewayResult.PaymentDetail toResult (){
        return new PaymentGatewayResult.PaymentDetail(
                transactionKey,
                orderId,
                amount,
                status,
                reason
        );
    }
}
