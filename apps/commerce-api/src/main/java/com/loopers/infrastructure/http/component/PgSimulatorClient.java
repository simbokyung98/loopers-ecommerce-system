package com.loopers.infrastructure.http.component;

import com.loopers.application.payment.dto.PaymentGatewayResult;
import com.loopers.application.payment.port.PaymentGatewayClient;
import com.loopers.infrastructure.http.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class PgSimulatorClient implements PaymentGatewayClient {

    private final RestClient pgRestClient;
    private final RestClient pgFastRestClient;

    // 추가 생성자로 fast 클라이언트 생성
    public PgSimulatorClient(RestClient pgRestClient,
                             RestClient.Builder builder,
                             @Value("${external.pg.base-url}") String baseUrl) {
        this.pgRestClient = pgRestClient;

        // 300ms 타임아웃 전용 요청 팩토리
        SimpleClientHttpRequestFactory fastFactory = new SimpleClientHttpRequestFactory();
        fastFactory.setConnectTimeout(200); // ms
        fastFactory.setReadTimeout(300);    // ms

        // 기본과 동일한 헤더 유지
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        this.pgFastRestClient = builder
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.addAll(headers))
                .requestFactory(fastFactory)
                .build();
    }


    @Override
    public PaymentGatewayResult.PaymentCreate requestPayment(PaymentGateWayCreateRequest request, String companyId){
        ResponseEntity<PaymentCreateResponse> http = pgFastRestClient.post()
                .uri("/api/v1/payments")
                .header("X-USER-ID",companyId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(PaymentCreateResponse.class);

        PaymentCreateResponse body = http.getBody();

        return body.toResult();


    }
    @Override
    public PaymentGatewayResult.PaymentDetail getPayment(String txKey, String companyId){
        ResponseEntity<PaymentDetailResponse> http =  pgRestClient.get()
                .uri("/api/v1/payments/{txKey}",txKey)
                .header("X-USER-ID", companyId)
                .retrieve()
                .toEntity(PaymentDetailResponse.class);

        PaymentDetailResponse body = http.getBody();

        return body.toResult();

    }
    @Override
    public PaymentGatewayResult.PaymentSummary getPaymentByOrderId(String orderId, String companyId){
        ResponseEntity<PaymentSummaryResponse> http =  pgRestClient.get()
                .uri(uri -> uri.path("/api/v1/payments")
                        .queryParam("orderId", orderId)
                        .build())
                .header("X-USER-ID", companyId)
                .retrieve()
                .toEntity(PaymentSummaryResponse.class);
        PaymentSummaryResponse body = http.getBody();

        return body.toResult();
    }
}
