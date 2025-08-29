package com.loopers.application.payment;


import com.loopers.application.payment.dto.PaymentGatewayResult;
import com.loopers.application.payment.dto.PaymentProbe;
import com.loopers.application.payment.port.PaymentGatewayClient;
import com.loopers.infrastructure.http.dto.PaymentCreateRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Service
@Slf4j
public class PaymentGatewayService {

    private static final String COMPANY_ID = "loopers-ecommerce";
    private static final String CALLBACK_URL = "http://localhost:8080/api/v1/pg/callback";

    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentGatewayService(
            PaymentGatewayClient paymentGatewayClient

    ) {
        this.paymentGatewayClient = paymentGatewayClient;
    }
    public PaymentProbe checkPayment(String txKey){
        try {
            PaymentGatewayResult.PaymentDetail detail =
                    paymentGatewayClient.getPayment(txKey, COMPANY_ID);

            String status = (detail == null) ? null : detail.status();
            if (status != null && "SUCCESS".equalsIgnoreCase(status.trim())) {
                return new PaymentProbe(PaymentProbe.Decision.CONFIRMED);
            }

            if (status != null && "PENDING".equalsIgnoreCase(status.trim())) {
                return new PaymentProbe(PaymentProbe.Decision.RETRY);
            }

            return new PaymentProbe(PaymentProbe.Decision.STOP);

        } catch (RestClientResponseException e) {
            // HTTP 응답 존재
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("PG 4xx on getPayment: txKey={}, status={}, body={}",
                        txKey, e.getStatusCode(), e.getResponseBodyAsString());
                return new PaymentProbe(PaymentProbe.Decision.STOP);
            }
            log.warn("PG 5xx on getPayment: txKey={}, status={}, body={}",
                    txKey, e.getStatusCode(), e.getResponseBodyAsString());
            return new PaymentProbe(PaymentProbe.Decision.RETRY);
        } catch (RestClientException e) {
            // 타임아웃/전송오류 등
            log.warn("PG timeout/transport on getPayment: txKey={}, cause={}", txKey, e.toString());
            return new PaymentProbe(PaymentProbe.Decision.RETRY);

        }
    }

    @CircuitBreaker(name = "pgCircuit")
    @Retry(name = "pgRetry", fallbackMethod = "pgCreateFallback")
    public String requestWithStatusRecovery(PaymentCreateRequest req) {

        try{
            PaymentGatewayResult.PaymentCreate request = paymentGatewayClient.requestPayment(req.toGateWayRequest(CALLBACK_URL), COMPANY_ID);


            log.info("PG create OK: orderId={}, txKey={}, status={}",
                    req.orderId(), request.transactionKey(), request.status());
            return request.transactionKey();

        }catch (RestClientResponseException e) {
            // HTTP 응답 존재 → 4xx/5xx 분기
            if (e.getStatusCode().is4xxClientError()) {
                String body = safeBody(e);
                log.warn("PG 4xx on create (business declined): orderId={}, status={}, body={}",
                        req.orderId(), e.getStatusCode(), body);
                throw new CoreException(ErrorType.BAD_REQUEST, body); // 재시도/서킷 제외
            }
            if (e.getStatusCode().is5xxServerError()) {
                log.warn("PG 5xx on create: orderId={}, status={}, body={}",
                        req.orderId(), e.getStatusCode(), safeBody(e));
                throw e; // 재시도/서킷 대상
            }
            log.warn("PG unexpected HTTP on create: orderId={}, status={}, body={}",
                    req.orderId(), e.getStatusCode(), safeBody(e));
            throw new CoreException(ErrorType.INTERNAL_ERROR, safeBody(e));

        } catch (RestClientException e) {
            // 타임아웃/전송오류 등
            if (isTimeoutLike(e)) {
                log.warn("PG timeout/transport error on create: orderId={}, cause={}", req.orderId(), e.toString());

                // 지연성 성공 탐지
                String txKey = probeTxKeyByOrderId(req.orderId());
                if (txKey != null) {
                    log.info("PG late success detected after timeout: orderId={}, txKey={}", req.orderId(), txKey);
                    return txKey;
                }
                throw e; // 재시도/서킷 대상
            }

            log.warn("PG unexpected RestClientException on create: orderId={}, cause={}", req.orderId(), e.toString());
            throw new CoreException(ErrorType.INTERNAL_ERROR, e.getMessage());

        } catch (RuntimeException e) {
            log.warn("PG unexpected error on create: orderId={}, cause={}", req.orderId(), e.toString());
            throw new CoreException(ErrorType.INTERNAL_ERROR, e.getMessage());
        }

    }

    /** 재시도까지 실패했을 때 호출 */
    @SuppressWarnings("unused")
    private String pgCreateFallback(PaymentCreateRequest req, Throwable cause) {
        if (cause instanceof CoreException core) {
            throw core; // 400 등 비즈니스 예외는 그대로 유지
        }
        if (cause instanceof HttpClientErrorException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, e.getResponseBodyAsString());
        }
        throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 통신 장애");
    }

    private String probeTxKeyByOrderId(String orderId){
        PaymentGatewayResult.PaymentSummary summary = paymentGatewayClient.getPaymentByOrderId(orderId, COMPANY_ID);

        if (summary == null || summary.transactions() == null || summary.transactions().isEmpty()) {
            return null;
        }
        return summary.transactions().get(0).transactionKey();

    }

    private static boolean isTimeoutLike(Throwable t) {
        Throwable root = NestedExceptionUtils.getRootCause(t);
        return (root instanceof java.net.SocketTimeoutException)
                || (root instanceof java.net.ConnectException)
                || (root instanceof java.net.SocketException);
    }

    private static String safeBody(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        return (body != null && !body.isBlank()) ? body : e.getMessage();
    }
}
