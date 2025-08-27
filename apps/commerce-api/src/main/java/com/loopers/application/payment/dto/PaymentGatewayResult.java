package com.loopers.application.payment.dto;


import com.loopers.infrastructure.http.dto.TransactionResponse;

import java.util.List;

public class PaymentGatewayResult {

    public record PaymentCreate (
            String transactionKey,
            String status,
            String result,
            String errorCode,
            String message
    ) {}

    public record PaymentSummary (
            String orderId,
            List<TransactionResponse> transactions
    ){}

    public record PaymentDetail(
            String transactionKey,
            String orderId,
            Long amount,
            String status,
            String reason

    ) {
    }


}
