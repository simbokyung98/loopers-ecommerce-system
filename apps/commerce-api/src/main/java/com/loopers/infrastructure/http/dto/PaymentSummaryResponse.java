package com.loopers.infrastructure.http.dto;


import com.loopers.application.payment.dto.PaymentGatewayResult;

import java.util.List;

public record PaymentSummaryResponse(
        String orderId,
        List<TransactionResponse> transactions
) {
    public PaymentGatewayResult.PaymentSummary toResult(){
        return new PaymentGatewayResult.PaymentSummary(
                orderId,
                transactions
        );
    }
}
