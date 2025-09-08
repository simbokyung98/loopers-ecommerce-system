package com.loopers.application.product;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.domain.Like.LikeRepository;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ProductFacadeConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private KafkaTemplate<Object, Object> kafkaTemplate;



    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @DisplayName("동시에 좋아요를 요청해도 상품의 좋아요 수가 정확히 반영되어야 한다.")
    @Test
    void concurrencyTest_likeCountShouldBeAccurate_whenMultipleUsersLikeSimultaneously() throws InterruptedException {
        BrandModel brand = brandRepository.saveBrand(new BrandModel("테스트 브랜드"));
        ProductModel product = productRepository.saveProduct(
                new ProductModel("비관적 락 테스트 상품", 100L, 1000L, ProductStatus.SELL, brand.getId())
        );

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 동시성 극대화
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    UserModel user = userRepository.save(
                            new UserModel("user" + index, "F", "1990-01-01", "user" + index + "@example.com")
                    );
                    likeFacade.like(LikeCriteria.Like.of(user.getId(), product.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    // 디버그용 로그만 남기고 넘김
                    System.err.println("실패한 예외 (thread: " + Thread.currentThread().getName() + "): " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 핵심: 이벤트/캐시 버스트 등 비동기 후처리가 끝날 때까지 기다린다.
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(50))
                .untilAsserted(() -> {
                    ProductModel info = productRepository.getProduct(product.getId()).orElseThrow();
                    assertAll(
                            () -> assertThat(successCount.get()).isEqualTo(threadCount),
                            () -> assertThat(failCount.get()).isEqualTo(0),
                            () -> assertThat(info.getLikeCount())
                                    .as("좋아요 수는 실행 수와 동일해야 한다.")
                                    .isEqualTo(threadCount)
                    );
                });


    }
}
