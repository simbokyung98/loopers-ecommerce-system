package com.loopers.application.coupon;

import com.loopers.application.coupon.dto.CouponCriteria;
import com.loopers.application.coupon.dto.CouponInfo;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class CouponFacadeIntegrationTest {

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("쿠폰을 발행할 때")
    @Nested
    class Issue{
        @DisplayName("쿠폰 발행 요청 시 정상적으로 발행된다.")
        @Test
        void issueCouponSuccessfully() {
            UserModel user = userRepository.save(
                    new UserModel("testId", "M", "2024-01-01", "test@example.com")
            );


            CouponModel coupon = couponRepository.saveCoupon(
                    new FixedCouponModel("정상 쿠폰", 100L, ZonedDateTime.now().plusDays(1), 5L)
            );

            CouponCriteria.Issue criteria = new CouponCriteria.Issue(user.getId(), coupon.getId());

            //act
            CouponInfo.IssuedCoupon result = couponFacade.issue(criteria);

            //assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.userId()).isEqualTo(user.getId()),
                    () -> assertThat(result.couponId()).isEqualTo(coupon.getId())
            );


        }
    }
}
