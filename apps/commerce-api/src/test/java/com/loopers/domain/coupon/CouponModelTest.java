package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.PercentageCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponModelTest {

    @Nested
    @DisplayName("정액 쿠폰 생성 및 사용 시, ")
    class FixedCoupon {

        @Test
        @DisplayName("할인 금액이 0 이하이면 생성 시 BAD_REQUEST 예외가 발생한다")
        void throwBadRequest_whenValueInvalid() {
            assertThatThrownBy(() ->
                    new FixedCouponModel("할인 쿠폰", 10L, ZonedDateTime.now().plusDays(1), 0L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문 금액보다 할인 금액이 작을 때 그대로 반환한다")
        void ApplyFixedDiscount_whenUnderOrderAmount() {
            FixedCouponModel coupon = new FixedCouponModel("정액 1000원", 100L, ZonedDateTime.now().plusDays(1), 1000L);
            Long discount = coupon.calculateDiscount(5000L);
            assertThat(discount).isEqualTo(1000L);
        }

        @Test
        @DisplayName("할인 금액이 주문 금액보다 클 경우 주문 금액만큼만 할인한다")
        void notExceedOrderAmount() {
            FixedCouponModel coupon = new FixedCouponModel("정액 10000원", 100L, ZonedDateTime.now().plusDays(1), 10000L);
            Long discount = coupon.calculateDiscount(3000L);
            assertThat(discount).isEqualTo(3000L);
        }

        @Test
        @DisplayName("만료된 쿠폰일 경우, BAD_REQUEST 예외를 던진다")
        void throwBadRequest_whenExpired() {
            FixedCouponModel coupon = new FixedCouponModel("만료됨", 100L, ZonedDateTime.now().minusDays(1), 1000L);
            assertThatThrownBy(() -> coupon.calculateDiscount(5000L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문 금액이 0 이하일 경우, BAD_REQUEST 예외를 던진다")
        void throwBadRequest_whenOrderAmountInvalid() {
            FixedCouponModel coupon = new FixedCouponModel("정액 쿠폰", 100L, ZonedDateTime.now().plusDays(1), 1000L);
            assertThatThrownBy(() -> coupon.calculateDiscount(0L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("정률 쿠폰 생성 및 사용시,")
    class PercentageCoupon {

        @Test
        @DisplayName("할인율이 0 이하이면, BAD_REQUEST 예외를 던진다")
        void ThrowBadRequest_whenDiscountRateIsZeroOrNegative() {
            assertThatThrownBy(() ->
                    new PercentageCouponModel("잘못된 쿠폰", 10L, ZonedDateTime.now().plusDays(1), 0L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("할인율이 100을 초과하면, BAD_REQUEST 예외를 던진다")
        void ThrowBadRequest_whenCountRateExceeds100() {
            assertThatThrownBy(() ->
                    new PercentageCouponModel("200% 쿠폰", 10L, ZonedDateTime.now().plusDays(1), 200L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("10프로 할인이 가능한 정률 쿠폰 사용시, 정률 계산이 정확하게 이루어진다")
        void calculatePercentageDiscountCorrectly() {
            PercentageCouponModel coupon = new PercentageCouponModel("10% 할인", 100L, ZonedDateTime.now().plusDays(1), 10L);
            Long discount = coupon.calculateDiscount(10000L);
            assertThat(discount).isEqualTo(1000L);
        }

        @Test
        @DisplayName("만료된 정률 쿠폰을 사용할 경우, BAD_REQUEST 예외를 던진다")
        void ThrowBadRequest_whenExpired() {
            PercentageCouponModel coupon = new PercentageCouponModel("만료됨", 100L, ZonedDateTime.now().minusDays(1), 10L);
            assertThatThrownBy(() -> coupon.calculateDiscount(1000L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문 금액이 0 이하이면, BAD_REQUEST 예외를 던진다")
        void ThrowBadRequest_whenOrderAmountInvalid() {
            PercentageCouponModel coupon = new PercentageCouponModel("10% 할인", 100L, ZonedDateTime.now().plusDays(1), 10L);
            assertThatThrownBy(() -> coupon.calculateDiscount(0L))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("쿠폰 발행시,")
    class IssueCoupon {

        @Test
        @DisplayName("유효기간이 남아있고, 수량이 남아있으면 정상 발행된다")
        void IssueSuccessfully() {
            CouponModel coupon = new FixedCouponModel("정상 쿠폰", 100L, ZonedDateTime.now().plusDays(1), 5L);

            coupon.issue();

            assertThat(coupon.getIssuedCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("만료된 쿠폰은 발행시 CONFLICT 예외가 발생한다")
        void throwConflict_whenCouponExpired() {
            CouponModel coupon = new FixedCouponModel("만료 쿠폰", 0L, ZonedDateTime.now().minusDays(1), 5L);

            assertThatThrownBy(coupon::issue)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.CONFLICT);
        }

        @Test
        @DisplayName("발급 한도에 도달한 쿠폰은 CONFLICT 예외가 발생한다")
        void throwConflict_whenIssuedLimitExceeded() {
            CouponModel coupon = new FixedCouponModel("한도 초과", 1L, ZonedDateTime.now().plusDays(1), 5L);

            coupon.issue();

            assertThatThrownBy(coupon::issue)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.CONFLICT);
        }
    }
}
