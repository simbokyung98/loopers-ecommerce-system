package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentGatewayResult;
import com.loopers.application.payment.port.PaymentGatewayClient;
import com.loopers.infrastructure.http.dto.PaymentCreateRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        // ===== Retry: 최초 + 1회 (총 2회), 대기 최소화 =====
        "resilience4j.retry.instances.pgRetry.max-attempts=2",
        "resilience4j.retry.instances.pgRetry.wait-duration=50ms",
        // 타임아웃/네트워크/5xx 등 RestClientException 포괄
        "resilience4j.retry.instances.pgRetry.retry-exceptions[0]=org.springframework.web.client.RestClientException",
        // 4xx/업무거절(CoreException)은 재시도 제외
        "resilience4j.retry.instances.pgRetry.ignore-exceptions[0]=org.springframework.web.client.HttpClientErrorException",
        "resilience4j.retry.instances.pgRetry.ignore-exceptions[1]=com.loopers.support.error.CoreException",
        "resilience4j.retry.instances.pgRetry.fail-after-max-attempts=true",

        // ===== CircuitBreaker: 작게/빨리 열리도록 =====
        "resilience4j.circuitbreaker.instances.pgCircuit.sliding-window-type=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.pgCircuit.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.pgCircuit.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.pgCircuit.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.pgCircuit.wait-duration-in-open-state=500ms",
        "resilience4j.circuitbreaker.instances.pgCircuit.permitted-number-of-calls-in-half-open-state=2",
        "resilience4j.circuitbreaker.instances.pgCircuit.slow-call-duration-threshold=2s",
        "resilience4j.circuitbreaker.instances.pgCircuit.slow-call-rate-threshold=50",
        // 5xx/타임아웃 등 RestClientException 기록
        "resilience4j.circuitbreaker.instances.pgCircuit.record-exceptions[0]=org.springframework.web.client.RestClientException",
        // 4xx/업무거절은 무시
        "resilience4j.circuitbreaker.instances.pgCircuit.ignore-exceptions[0]=org.springframework.web.client.HttpClientErrorException",
        "resilience4j.circuitbreaker.instances.pgCircuit.ignore-exceptions[1]=com.loopers.support.error.CoreException"
})
class PaymentGatewayServiceCircuitTest {

    @Autowired
    PaymentGatewayService sut;

    @MockitoBean
    PaymentGatewayClient paymentGatewayClient;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("pgCircuit");
        cb.reset(); // CLOSED로 초기화
    }

    // ===== Helpers =====

    private PaymentCreateRequest req(String orderId) {
        // 현재 프로젝트의 PaymentCreateRequest 시그니처: (orderId, cardType, cardNo, amount)
        // 실제 객체/모킹 둘 다 가능하지만, 여기서는 orderId만 필요한 간단 mock 사용
        PaymentCreateRequest r = mock(PaymentCreateRequest.class);
        when(r.orderId()).thenReturn(orderId);
        return r;
    }

    private PaymentGatewayResult.PaymentCreate success(String txKey) {
        // 프로젝트 DTO 시그니처에 맞게 생성
        // (txKey, statusMsg, result, ... ) 형태라 가정
        return new PaymentGatewayResult.PaymentCreate(txKey, "OK", "SUCCESS", null, null);
    }

    private HttpClientErrorException badRequestEx(String message) {
        String body = "{\"meta\":{\"result\":\"FAIL\",\"errorCode\":\"Bad Request\",\"message\":\"" + message + "\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request",
                headers, body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
    }

    private HttpServerErrorException serverErrorEx(String msg) {
        String body = "{\"meta\":{\"message\":\"" + msg + "\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR, "ISE",
                headers, body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
    }



    @Test
    @DisplayName("성공(SUCCESS)이면 트랜잭션키를 반환한다")
    void success_returnsTxKey() {
        PaymentCreateRequest request = req("ORD-OK");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenReturn(success("tx-123"));

        String tx = sut.requestWithStatusRecovery(request);

        assertThat(tx).isEqualTo("tx-123");
        verify(paymentGatewayClient, times(1)).requestPayment(any(), anyString());
        verify(paymentGatewayClient, never()).getPaymentByOrderId(anyString(), anyString());
    }

    @Test
    @DisplayName("5xx면 1회 재시도 후 성공 시 트랜잭션키를 반환한다")
    void serverError_retry_then_success() {
        PaymentCreateRequest request = req("ORD-5XX");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenThrow(serverErrorEx("PG 장애1"))
                .thenReturn(success("tx-999"));

        String tx = sut.requestWithStatusRecovery(request);

        assertThat(tx).isEqualTo("tx-999");
        verify(paymentGatewayClient, times(2)).requestPayment(any(), anyString());
        verify(paymentGatewayClient, never()).getPaymentByOrderId(anyString(), anyString());
    }

    @Test
    @DisplayName("5xx가 연속 발생하면 fallback을 통해 CoreException(INTERNAL_ERROR)을 던진다")
    void serverError_retry_then_fallback() {
        PaymentCreateRequest request = req("ORD-5XX-FAIL");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenThrow(serverErrorEx("PG 장애1"))
                .thenThrow(serverErrorEx("PG 장애2"));

        assertThatThrownBy(() -> sut.requestWithStatusRecovery(request))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.INTERNAL_ERROR));

        verify(paymentGatewayClient, times(2)).requestPayment(any(), anyString());
        verify(paymentGatewayClient, never()).getPaymentByOrderId(anyString(), anyString());
    }

    @Test
    @DisplayName("타임아웃: 선조회(hit)면 재시도 없이 트랜잭션키를 반환한다")
    void timeout_probe_hit_no_retry() {
        PaymentCreateRequest request = req("ORD-TO-HIT");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenThrow(new RestClientException("timeout", new SocketTimeoutException("read")));

        // summary.transactions().get(0).transactionKey() 체인 스텁
        PaymentGatewayResult.PaymentSummary summary =
                mock(PaymentGatewayResult.PaymentSummary.class, RETURNS_DEEP_STUBS);
        when(summary.transactions().isEmpty()).thenReturn(false);
        when(summary.transactions().get(0).transactionKey()).thenReturn("tx-probed");

        when(paymentGatewayClient.getPaymentByOrderId(eq("ORD-TO-HIT"), eq("loopers-ecommerce")))
                .thenReturn(summary);

        String tx = sut.requestWithStatusRecovery(request);

        assertThat(tx).isEqualTo("tx-probed");
        verify(paymentGatewayClient, times(1)).requestPayment(any(), anyString()); // 재시도 없음
        verify(paymentGatewayClient, times(1)).getPaymentByOrderId(eq("ORD-TO-HIT"), anyString());
    }

    @Test
    @DisplayName("타임아웃: 선조회(miss)면 1회 재시도 후 성공 시 트랜잭션키를 반환한다")
    void timeout_probe_miss_then_retry_success() {
        PaymentCreateRequest request = req("ORD-TO-MISS");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenThrow(new RestClientException("timeout", new SocketTimeoutException("r1")))
                .thenReturn(success("tx-after-retry"));

        PaymentGatewayResult.PaymentSummary emptySummary =
                mock(PaymentGatewayResult.PaymentSummary.class, RETURNS_DEEP_STUBS);
        when(emptySummary.transactions().isEmpty()).thenReturn(true);

        when(paymentGatewayClient.getPaymentByOrderId(eq("ORD-TO-MISS"), eq("loopers-ecommerce")))
                .thenReturn(emptySummary);

        String tx = sut.requestWithStatusRecovery(request);

        assertThat(tx).isEqualTo("tx-after-retry");
        verify(paymentGatewayClient, times(2)).requestPayment(any(), anyString()); // 재시도 1회
        verify(paymentGatewayClient, times(1)).getPaymentByOrderId(eq("ORD-TO-MISS"), anyString());
    }

    @Test
    @DisplayName("4xx면 재시도 없이 CoreException(BAD_REQUEST)을 던진다")
    void clientError_badRequest() {
        // 실 객체를 사용해도 됨 (toGateWayRequest 호출 안전)
        PaymentCreateRequest request =
                new PaymentCreateRequest("999", "SAMSU", "1234-5678-9814-1451", "1000");

        when(paymentGatewayClient.requestPayment(any(), eq("loopers-ecommerce")))
                .thenThrow(badRequestEx("필드 'cardType'의 값 'SAMSU'..."));

        assertThatThrownBy(() -> sut.requestWithStatusRecovery(request))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.BAD_REQUEST));

        verify(paymentGatewayClient, times(1)).requestPayment(any(), anyString()); // 재시도 없음
        verify(paymentGatewayClient, never()).getPaymentByOrderId(anyString(), anyString());
    }

    @Test
    @DisplayName("연속 5xx로 실패율>50%가 되면 Open → 다음 호출은 즉시 차단(fallback)")
    void opens_then_shortCircuits() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("pgCircuit");

        when(paymentGatewayClient.requestPayment(any(), anyString()))
                .thenThrow(serverErrorEx("PG 장애"));

        // 최소 표본(5) 채우며 실패율 100% → Open(각 호출은 fallback으로 CoreException(INTERNAL_ERROR))
        for (int i = 1; i <= 5; i++) {
            PaymentCreateRequest request = req("ORD-FAIL-" + i);
            assertThatThrownBy(() -> sut.requestWithStatusRecovery(request))
                    .isInstanceOf(CoreException.class)
                    .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                            .isEqualTo(ErrorType.INTERNAL_ERROR));
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Open 상태에서 한 번 더 호출 → 즉시 차단(fallback), 클라이언트 미호출
        reset(paymentGatewayClient);
        PaymentCreateRequest blocked = req("ORD-BLOCKED");
        assertThatThrownBy(() -> sut.requestWithStatusRecovery(blocked))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.INTERNAL_ERROR));
        verify(paymentGatewayClient, never()).requestPayment(any(), anyString());
    }

    @Test
    @DisplayName("Open→Half-Open 대기 후 허용 2건이 성공하면 Close 로 복구된다")
    void halfOpen_allows_two_success_then_closes() throws InterruptedException {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("pgCircuit");

        // 먼저 Open 상태로 만든다
        when(paymentGatewayClient.requestPayment(any(), anyString()))
                .thenThrow(serverErrorEx("PG 장애"));
        for (int i = 1; i <= 5; i++) {
            try { sut.requestWithStatusRecovery(req("ORD-FAIL-" + i)); }
            catch (CoreException ignore) {}
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Open 유지시간 경과 → Half-Open
        Thread.sleep(600); // wait-duration-in-open-state=500ms 보다 길게

        // Half-Open에서 2건 성공 → Close 복구
        reset(paymentGatewayClient);
        when(paymentGatewayClient.requestPayment(any(), anyString()))
                .thenReturn(new PaymentGatewayResult.PaymentCreate("tx-1", "OK", "SUCCESS", null, null))
                .thenReturn(new PaymentGatewayResult.PaymentCreate("tx-2", "OK", "SUCCESS", null, null));

        String tx1 = sut.requestWithStatusRecovery(req("ORD-SUCCESS-1"));
        String tx2 = sut.requestWithStatusRecovery(req("ORD-SUCCESS-2"));

        assertThat(tx1).isEqualTo("tx-1");
        assertThat(tx2).isEqualTo("tx-2");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
