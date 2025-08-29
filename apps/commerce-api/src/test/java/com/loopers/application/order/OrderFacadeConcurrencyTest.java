package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("여러명의 유저가 동일한 상품을 동시에 주문해도 상품의 재고가 정확히 차감된다.")
    @Test
    void concurrencyTest_stockShouldBeProperlyDecreased() throws InterruptedException {

        ProductModel product = productRepository.saveProduct(
                new ProductModel("테스트 상품", 10L, 100L, ProductStatus.SELL, 1L)
        );


        int threadCount = 10; // 재고 수량과 동일
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // act
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    UserModel user = userRepository.save(
                            new UserModel("testId" + idx, "M", "2024-01-01", "test@example.com")
                    );
                    PointModel point = pointRepository.save(new PointModel(user.getId()));
                    point.charge(20000L);
                    pointRepository.save(point);

                    OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                            user.getId(),
                            null,
                            "서울시 어딘가",
                            "01012345678",
                            "홍길동",
                            PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444",
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
        executor.shutdown();

        // assert
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
/*TODO : 포인트 위치 변경 추후 동시성 테스트 추가*/

//    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
//    @Test
//    void pointShouldBeDeductedSequentiallyByPessimisticLock() throws InterruptedException {
//
//        UserModel user = userRepository.save(new UserModel("testId2", "F", "1990-01-01", "user@example.com"));
//        PointModel point = new PointModel(user.getId());
//        point.charge(10000L); // 10회 주문 가능
//        pointRepository.save(point);
//
//        ProductModel product = productRepository.saveProduct(
//                new ProductModel("비관적 락 테스트 상품", 100L, 1000L, ProductStatus.SELL, 100L)
//        );
//
//
//        int totalThreads = 20;
//        int maxAffordableOrders = 10; // 10,000 ÷ 1,000
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        CountDownLatch latch = new CountDownLatch(totalThreads);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//        OrderCriteria.Order orderRequest = new OrderCriteria.Order(
//                user.getId(),
//                 null,
//                "서울시 루퍼스",
//                "01012345678",
//                "비관적락유저",
//                List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
//        );
//
//        // act
//        for (int i = 0; i < totalThreads; i++) {
//            executorService.submit(() -> {
//                try {
//                    orderFacade.order(orderRequest); // 포인트 차감 포함
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                    System.err.println("실패한 주문: " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//        executorService.shutdown();
//
//        //assert
//        PointModel updatedPoint = pointRepository.findByUserId(user.getId()).orElseThrow();
//
//        assertAll(
//                () -> assertThat(successCount.get())
//                        .as("최대 주문 가능 횟수만큼만 성공해야 함")
//                        .isEqualTo(maxAffordableOrders),
//                () -> assertThat(failCount.get())
//                        .as("잔액 부족으로 인해 실패한 주문 수")
//                        .isEqualTo(totalThreads - maxAffordableOrders),
//                () -> assertThat(updatedPoint.getTotalAmount())
//                        .as("10회 주문 후 포인트 잔액은 0이어야 함")
//                        .isEqualTo(0)
//        );
//    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void onlyOneOrderShouldUseCouponUnderConcurrency_withOptimisticLock() throws InterruptedException{

        UserModel user = userRepository.save(new UserModel("testId2", "F", "1990-01-01", "user@example.com"));
        PointModel point = new PointModel(user.getId());
        point.charge(10000L); // 10회 주문 가능
        pointRepository.save(point);

        ProductModel product = productRepository.saveProduct(
                new ProductModel("테스트 상품", 100L, 1000L, ProductStatus.SELL, 100L)
        );

        CouponModel coupon = couponRepository.saveCoupon(
                new FixedCouponModel("정상 쿠폰", 5L, ZonedDateTime.now().plusDays(1), 5L)
        );
        IssuedCouponModel issuedCoupon = issuedCouponRepository.saveIssuedCoupon(new IssuedCouponModel(coupon.getId(), user.getId()));

        int totalThreads = 10;

        CountDownLatch latch = new CountDownLatch(totalThreads);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                orderFacade.order(new OrderCriteria.Order(
                        user.getId(),
                        issuedCoupon.getId(),//동일 쿠폰 사용
                        "서울시 멀티디바이스",
                        "01012345678",
                        "동시성유저",
                        PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444",
                        List.of(new OrderCriteria.ProductQuantity(product.getId(), 1L))
                ));
                successCount.incrementAndGet();

            } catch (Exception e) {
                failCount.incrementAndGet();
                System.err.println("주문 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            } finally {
                latch.countDown();
            }
        };

        // when
        for (int i = 0; i < totalThreads; i++) {
            executor.submit(task);
        }

        latch.await();
        executor.shutdown();

        IssuedCouponModel updatedCoupon = issuedCouponRepository.getIssuedCoupon(issuedCoupon.getId()).orElseThrow();

        assertAll(
                () -> assertThat(successCount.get())
                        .as("쿠폰은 한번만 사용되어야 하기 때문에, 성공한 주문은 하나.")
                        .isEqualTo(1),
                () -> assertThat(failCount.get())
                        .as("나머지 주문은 쿠폰 중복 사용 오류로 실패해야 함")
                        .isEqualTo(totalThreads - 1),
                () -> assertThat(updatedCoupon.getUsedAt())
                        .as("사용 시각이 존재")
                        .isNotNull()
        );







    }

}
