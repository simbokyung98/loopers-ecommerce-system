package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
 class OrderFacadeConcurrencyTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @PersistenceContext
    EntityManager em;

    @DisplayName("동시에 주문해도 재고가 정확히 차감된다.")
    @Test
    void concurrencyTest_stockShouldBeProperlyDecreased() throws InterruptedException {
        // given
        ProductModel product = productRepository.saveProduct(
                new ProductModel("테스트 상품", 10L, 100L, ProductStatus.SELL, 1L)
        );


        int threadCount = 10; // 재고 수량과 동일
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    // ✅ 매 요청마다 유저/포인트 새로 생성
                    UserModel user = userRepository.save(
                            new UserModel("testId" + idx, "M", "2024-01-01", "test@example.com")
                    );
                    PointModel point = pointRepository.save(new PointModel(user.getId()));
                    point.charge(20000L);
                    pointRepository.save(point);

                    OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                            user.getId(),
                            "서울시 어딘가",
                            "01012345678",
                            "홍길동",
                            List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
                    );

                    orderFacade.order(orderRequest);


                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        ProductModel updatedProduct = productRepository.getProduct(product.getId()).orElseThrow();

        assertThat(successCount.get())
                .as("성공한 주문 수는 재고 수량과 같아야 한다.")
                .isEqualTo(10);
        assertThat(failCount.get())
                .as("실패한 주문 수는 총 요청 수 - 재고 수량")
                .isEqualTo(0);
        assertThat(updatedProduct.getStock())
                .as("재고는 정확히 줄어야 한다.")
                .isEqualTo(0);
    }

    @DisplayName("동시에 주문해도 재고가 정확히 차감된다.")
    @Test
    void concurrencyTest_stockShouldBeProperlyDecreased_withCompletableFuture() {
        // given
        ProductModel product = productRepository.saveProduct(
                new ProductModel("테스트 상품", 10L, 100L, ProductStatus.SELL, 1L)
        );

        int threadCount = 10; // 재고 수량과 동일

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    // 재고 차감 로직
                    UserModel user = userRepository.save(
                            new UserModel("testId1", "M", "2024-01-01", "test@example.com")
                    );
                    PointModel point = pointRepository.save(new PointModel(user.getId()));
                    point.charge(20000L);
                    pointRepository.save(point);

                    OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                            user.getId(),
                            "서울시 어딘가",
                            "01012345678",
                            "홍길동",
                            List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
                    );

                    orderFacade.order(orderRequest);

                    successCount.incrementAndGet();
                }),
                CompletableFuture.runAsync(() -> {
                    // 재고 차감 로직
                    UserModel user = userRepository.save(
                            new UserModel("testId2", "M", "2024-01-01", "test@example.com")
                    );
                    PointModel point = pointRepository.save(new PointModel(user.getId()));
                    point.charge(20000L);
                    pointRepository.save(point);

                    OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                            user.getId(),
                            "서울시 어딘가",
                            "01012345678",
                            "홍길동",
                            List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
                    );

                    orderFacade.order(orderRequest);

                    successCount.incrementAndGet();
                })
        ).exceptionally(
                ex -> {
                     failCount.incrementAndGet();

                    throw new RuntimeException("실패: " + ex.getMessage());
                }
        ).join();

//        List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
//                .mapToObj(i -> CompletableFuture.runAsync(() -> {
//                    try {
//                        // ✅ 매 요청마다 유저/포인트 새로 생성
//
//                    } catch (Exception e) {
//
//                    }
//                })).toList();
//
//        // when
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // then
        ProductModel updatedProduct = productRepository.getProduct(product.getId()).orElseThrow();

        assertThat(successCount.get())
                .as("성공한 주문 수는 재고 수량과 같아야 한다.")
                .isEqualTo(2);

        assertThat(failCount.get())
                .as("실패한 주문 수는 총 요청 수 - 재고 수량")
                .isEqualTo(0);

        assertThat(updatedProduct.getStock())
                .as("재고는 정확히 줄어야 한다.")
                .isEqualTo(8);
    }


    @Test
    void test(){

        ProductModel product = productRepository.saveProduct(
                new ProductModel("테스트 상품", 10L, 100L, ProductStatus.SELL, 1L)
        );

        // ✅ 매 요청마다 유저/포인트 새로 생성
        UserModel user = userRepository.save(
                new UserModel("testId" + 1, "M", "2024-01-01", "test@example.com")
        );
        PointModel point = pointRepository.save(new PointModel(user.getId()));
        point.charge(20000L);
        pointRepository.save(point);

        OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                user.getId(),
                "서울시 어딘가",
                "01012345678",
                "홍길동",
                List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
        );

        orderFacade.order(orderRequest);
        orderFacade.order(orderRequest);

        ProductModel updatedProduct = productRepository.getProduct(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock())
                .as("재고는 정확히 줄어야 한다.")
                .isEqualTo(8);

    }

    @DisplayName("동일한 유저가 여러 기기에서 동시에 주문해도, 포인트가 중복 차감되지 않아야 한다.")
    @Test
    void pointShouldBeDeductedOncePerOrderEvenWhenMultipleDevicesRequestConcurrently() throws InterruptedException {
        // given
        UserModel user = userRepository.save(new UserModel("testId2", "F", "1990-01-01", "user@example.com"));
        PointModel point = new PointModel(user.getId());
        point.charge(10000L); // 포인트 10,000원 충전
        pointRepository.save(point);

        ProductModel product = productRepository.saveProduct(
                new ProductModel("동시성 포인트 테스트 상품", 1000L, 100L, ProductStatus.SELL, 100L)
        );

        int maxAffordableOrders = 10;
        int totalThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                            user.getId(),
                            "서울시 어딘가",
                            "01012345678",
                            "동시유저",
                            List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
                    );

                    orderFacade.order(orderRequest);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        PointModel updatedPoint = pointRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(successCount.get())
                .as("포인트가 허용하는 주문 수만큼만 성공해야 한다.")
                .isEqualTo(maxAffordableOrders);

        assertThat(failCount.get())
                .as("포인트 초과로 인해 실패한 주문 수.")
                .isEqualTo(totalThreads - maxAffordableOrders);

        assertThat(updatedPoint.getTotalAmount())
                .as("포인트는 정확히 10회 주문 후 0원이 되어야 한다.")
                .isEqualTo(0);
    }

}
