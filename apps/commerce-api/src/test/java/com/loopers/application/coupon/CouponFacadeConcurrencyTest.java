package com.loopers.application.coupon;

import com.loopers.application.coupon.dto.CouponCriteria;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponFacadeConcurrencyTest {

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private UserRepository userRepository;

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

    @DisplayName("서로 다른 유저가 동시에 쿠폰을 발행해도 쿠폰의 갯수가 정확히 차감되어야 한다.")
    @Test
    void deductCouponQuantityAccurately_whenDifferentUsersIssueSimultaneously() throws InterruptedException {

        CouponModel coupon = couponRepository.saveCoupon(
                new FixedCouponModel("정상 쿠폰", 5L, ZonedDateTime.now().plusDays(1), 5L)
        );

        int threadCount = 10;
        int maxIssued = 5;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    UserModel user = userRepository.save(
                            new UserModel("testId" + idx, "M", "2024-01-01", "test@example.com")
                    );

                    CouponCriteria.Issue criteria = new CouponCriteria.Issue(user.getId(), coupon.getId());

                    couponFacade.issue(criteria);

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

        //assert
        CouponModel couponModel  = couponRepository.getCoupon(coupon.getId()).orElseThrow();

        Assertions.assertAll(
                () -> assertThat(successCount.get())
                        .as("쿠폰 갯수 만큼함 성공해야한다.")
                        .isEqualTo(maxIssued),
                () -> assertThat(failCount.get())
                        .as("쿠폰 부족으로 실패한 횟수")
                        .isEqualTo(threadCount - maxIssued),
                () -> assertThat(couponModel.getIssuedCount())
                        .as("10회 쿠폰 발행 후 쿠폰 카운트 수는 5개 여야 한다")
                        .isEqualTo(5)
        );


    }
}
