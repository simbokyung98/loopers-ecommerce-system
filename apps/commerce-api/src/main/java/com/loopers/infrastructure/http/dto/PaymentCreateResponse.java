package com.loopers.infrastructure.http.dto;


import com.loopers.application.payment.dto.PaymentGatewayResult;

public record PaymentCreateResponse(
    Meta meta,
    Data data
) {
    public record Meta(String result, String errorCode, String message) {}
    public record Data(String transactionKey, String status) {}

    public PaymentGatewayResult.PaymentCreate toResult(){
        return new PaymentGatewayResult.PaymentCreate(
                data.transactionKey,
                data.status,
                meta.result,
                meta.errorCode,
                meta.message
        );
    }
}
