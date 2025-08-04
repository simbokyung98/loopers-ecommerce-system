package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
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

    @DisplayName("동일한 유저가 여러 기기에서 동시에 주문해도, 포인트가 중복 차감되지 않아야 한다.")
    @Test
    void concurrencyTest_pointShouldNotBeOverspent() throws InterruptedException {
        // given
        UserModel user = userRepository.save(new UserModel("testId2", "F", "1990-01-01", "user@example.com"));
        PointModel point = pointRepository.save(new PointModel(user.getId()));
        point.charge(10000L); // 포인트 1만 충전 (1000원짜리 상품 최대 10개 주문 가능)
        pointRepository.save(point);

        ProductModel product = productRepository.saveProduct(
                new ProductModel("포인트테스트상품", 1000L, 100L, ProductStatus.SELL, 100L)
        );

        int threadCount = 20; // 재고는 충분하지만 포인트는 10개까지만 허용
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
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
                    System.out.println("실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        PointModel updatedPoint = pointRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(successCount.get())
                .as("성공한 주문 수는 포인트가 허용하는 범위 내여야 한다.")
                .isEqualTo(10);
        assertThat(failCount.get())
                .as("실패한 주문 수는 포인트 부족으로 발생한다.")
                .isEqualTo(10);
        assertThat(updatedPoint.getTotalAmount())
                .as("남은 포인트는 0이어야 한다.")
                .isEqualTo(0);
    }



}
