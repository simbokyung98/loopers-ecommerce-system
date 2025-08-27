package com.loopers.infrastructure.component;

import com.loopers.infrastructure.http.component.PgSimulatorClient;
import com.loopers.infrastructure.http.dto.PaymentGateWayCreateRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class PgSimulatorClientTest {


    private MockWebServer server;

    private PgSimulatorClient newSut(String baseUrl) {
        RestClient pgRestClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .build();

        RestClient.Builder builder = RestClient.builder(); // fast 클라용 builder (생성자에서 baseUrl 세팅함)
        return new PgSimulatorClient(pgRestClient, builder, baseUrl);
    }

    private static PaymentGateWayCreateRequest req() {
        return new PaymentGateWayCreateRequest(
                "ORD-X", "SAMSUNG", "1111-2222-3333-4444", "10000", "http://cb"
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) server.shutdown();
    }

    @Test
    @DisplayName("requestPayment: 5xx면 HttpServerErrorException, 헤더 X-USER-ID 전송 확인")
    void requestPayment_5xx() throws Exception {
        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setResponseCode(500)); // 바디 없음
        String baseUrl = server.url("/").toString();

        PgSimulatorClient sut = newSut(baseUrl);

        assertThatThrownBy(() -> sut.requestPayment(req(), "cid"))
                .isInstanceOf(HttpServerErrorException.class);

        RecordedRequest r = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(r).isNotNull();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(r.getPath()).isEqualTo("/api/v1/payments");
        assertThat(r.getHeader("X-USER-ID")).isEqualTo("cid");
    }

    @Test
    @DisplayName("requestPayment: 서버가 응답을 지연하면(readTimeout=300ms) 타임아웃 발생")
    void requestPayment_timeout() throws Exception {
        server = new MockWebServer();
        server.start();

        server.enqueue(new MockResponse()
                .setHeadersDelay(5, TimeUnit.SECONDS) // readTimeout(300ms)보다 길게
                .addHeader("Content-Type", "application/json")
                .setBody("{}"));

        String baseUrl = server.url("/").toString();
        PgSimulatorClient sut = newSut(baseUrl);

        // ✅ 상위는 RestClientException, 루트는 SocketTimeoutException 으로 단언
        assertThatThrownBy(() -> sut.requestPayment(req(), "cid"))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }
    @Test
    @DisplayName("getPayment: 5xx면 HttpServerErrorException")
    void getPayment_5xx() throws Exception {
        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setResponseCode(500));
        String baseUrl = server.url("/").toString();

        PgSimulatorClient sut = newSut(baseUrl);

        assertThatThrownBy(() -> sut.getPayment("TX-ERR", "cid"))
                .isInstanceOf(HttpServerErrorException.class);

        RecordedRequest r = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(r).isNotNull();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(r.getPath()).isEqualTo("/api/v1/payments/TX-ERR");
        assertThat(r.getHeader("X-USER-ID")).isEqualTo("cid");
    }

    @Test
    @DisplayName("getPaymentByOrderId: 4xx면 HttpClientErrorException")
    void getPaymentByOrderId_4xx() throws Exception {
        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setResponseCode(400));
        String baseUrl = server.url("/").toString();

        PgSimulatorClient sut = newSut(baseUrl);

        assertThatThrownBy(() -> sut.getPaymentByOrderId("ORD-X", "cid"))
                .isInstanceOf(HttpClientErrorException.class);

        RecordedRequest r = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(r).isNotNull();
        assertThat(r.getMethod()).isEqualTo("GET");
        // MockWebServer는 쿼리까지 path에 포함함
        assertThat(r.getPath()).isEqualTo("/api/v1/payments?orderId=ORD-X");
        assertThat(r.getHeader("X-USER-ID")).isEqualTo("cid");
    }
}
