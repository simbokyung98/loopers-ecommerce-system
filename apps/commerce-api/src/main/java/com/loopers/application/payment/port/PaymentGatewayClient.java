package com.loopers.application.payment.port;


import com.loopers.application.payment.dto.PaymentGatewayResult;
import com.loopers.infrastructure.http.dto.PaymentGateWayCreateRequest;

public interface PaymentGatewayClient {
    PaymentGatewayResult.PaymentCreate requestPayment(PaymentGateWayCreateRequest request, String companyId);
    PaymentGatewayResult.PaymentDetail getPayment(String txKey, String companyId);
    PaymentGatewayResult.PaymentSummary getPaymentByOrderId(String orderId, String companyId);

}
