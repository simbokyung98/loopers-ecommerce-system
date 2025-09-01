package com.loopers.interfaces.api.like;


import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.repository.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LikeV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    private static final String BASE = "/api/v1/like";

    private Long userId;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() {
        // 유저
        UserModel user = userJpaRepository.save(
                new UserModel("testId", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com")
        );
        userId = user.getId();

        // 상품 2개
        ProductModel p1 = productRepository.saveProduct(
                new ProductModel("상품1", 0L, 0L, ProductStatus.SELL, 1L)
        );
        ProductModel p2 = productRepository.saveProduct(
                new ProductModel("상품2", 0L, 0L, ProductStatus.SELL, 1L)
        );
        productId1 = p1.getId();
        productId2 = p2.getId();
    }


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    private static final ParameterizedTypeReference<ApiResponse<Object>> VOID_BODY = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<LikeV1Dto.LikeProductsResponse>> LIKE_PRODUCTS_BODY = new ParameterizedTypeReference<>() {};

    private HttpHeaders headersWithUserId(Long id) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-USER-ID", String.valueOf(id));
        return h;
    }

    @Nested
    @DisplayName("POST /api/v1/like/products/{productId}")
    class Like {

        @Test
        @DisplayName("정상 요청 시 200 OK, 이후 GET에서 좋아요 목록에 포함된다")
        void like_thenAppearInList() {
            // when
            ResponseEntity<ApiResponse<Object>> resp = testRestTemplate.exchange(
                    BASE + "/products/" + productId1,
                    HttpMethod.POST,
                    new HttpEntity<>(null, headersWithUserId(userId)),
                    VOID_BODY
            );

            // then
            assertAll(
                    () -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(resp.getBody()).isNotNull(),
                    () -> assertThat(resp.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS)
            );

            // @Async AFTER_COMMIT로 집계/버전 갱신이 이루어지므로 잠깐 대기 후 GET 검증
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                ResponseEntity<ApiResponse<LikeV1Dto.LikeProductsResponse>> getResp = testRestTemplate.exchange(
                        BASE + "/products",
                        HttpMethod.GET,
                        new HttpEntity<>(headersWithUserId(userId)),
                        LIKE_PRODUCTS_BODY
                );
                assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(getResp.getBody().data().likeProducts().size()).isEqualTo(1);
                assertThat(getResp.getBody().data().likeProducts()).extracting("id").contains(productId1);
            });
        }

        @Test
        @DisplayName("X-USER-ID 헤더 없으면 400 Bad Request")
        void like_missingHeader_400() {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<ApiResponse<Object>> resp = testRestTemplate.exchange(
                    BASE + "/products/" + productId1,
                    HttpMethod.POST,
                    new HttpEntity<>(null, h),
                    VOID_BODY
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/like/products/{productId} (dislike)")
    class Dislike {

        @BeforeEach
        void likedOnce() {
            // 선행 좋아요 1회
            testRestTemplate.exchange(
                    BASE + "/products/" + productId1,
                    HttpMethod.POST,
                    new HttpEntity<>(null, headersWithUserId(userId)),
                    VOID_BODY
            );
            Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                ResponseEntity<ApiResponse<LikeV1Dto.LikeProductsResponse>> getResp = testRestTemplate.exchange(
                        BASE + "/products",
                        HttpMethod.GET,
                        new HttpEntity<>(headersWithUserId(userId)),
                        LIKE_PRODUCTS_BODY
                );
                assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            });
        }

        @Test
        @DisplayName("정상 요청 시 200 OK, 이후 GET에서 목록에서 빠진다")
        void dislike_thenRemovedFromList() {
            ResponseEntity<ApiResponse<Object>> resp = testRestTemplate.exchange(
                    BASE + "/products/" + productId1,
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, headersWithUserId(userId)),
                    VOID_BODY
            );
            assertAll(
                    () -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(resp.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS)
            );

            Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                ResponseEntity<ApiResponse<LikeV1Dto.LikeProductsResponse>> getResp = testRestTemplate.exchange(
                        BASE + "/products",
                        HttpMethod.GET,
                        new HttpEntity<>(headersWithUserId(userId)),
                        LIKE_PRODUCTS_BODY
                );
                assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(getResp.getBody().data().likeProducts()).extracting("id").doesNotContain(productId1);
            });
        }

        @Test
        @DisplayName("X-USER-ID 헤더 없으면 400 Bad Request")
        void dislike_missingHeader_400() {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<ApiResponse<Object>> resp = testRestTemplate.exchange(
                    BASE + "/products/" + productId1,
                    HttpMethod.DELETE,
                    new HttpEntity<>(null, h),
                    VOID_BODY
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }




}
