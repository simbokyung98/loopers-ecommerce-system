package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private IssuedCouponRepository issuedCouponRepository;

    @Mock
    private CouponRepository couponRepository;

    @DisplayName("쿠폰을 발행여부를 검증할 때,")
    @Nested
    class ValidateCoupon {

        @DisplayName("이미 발행된 쿠폰일 경우, BAD_REQUEST 예외가 발생한다")
        @Test
        void throwBadRequest_whenAlreadyIssued(){

            Long userId = 1L;
            Long couponId = 1L;

            IssuedCouponModel issuedCouponModel =
                    new IssuedCouponModel(userId, couponId);

            when(issuedCouponRepository.isCouponIssuedToUser(userId, couponId))
                    .thenReturn(true);

            CouponCommand.Issue command = new CouponCommand.Issue(userId, couponId);

            //act
            Throwable throwable = catchThrowable(() ->
                    couponService.validateCouponNotIssued(command)
            );

            //assert
            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("쿠폰을 발행할 때")
    @Nested
    class Issue {
        @Test
        @DisplayName("쿠폰 발행 성공 시, 쿠폰 수량이 증가하고 발급 이력이 저장된다")
        void issueCouponSuccessfully() {
            Long couponId = 1L;
            Long userId = 2L;

            CouponModel coupon = new FixedCouponModel("테스트 쿠폰", 5L, ZonedDateTime.now().plusDays(1), 1000L);
            IssuedCouponModel issued = new IssuedCouponModel(couponId, userId);

            when(couponRepository.findCouponByIdForUpdate(couponId))
                    .thenReturn(Optional.of(coupon));
            when(issuedCouponRepository.saveIssuedCoupon(any()))
                    .thenReturn(issued);

            CouponCommand.Issue command = new CouponCommand.Issue(userId, couponId);

            //act
            IssuedCouponModel result = couponService.issue(command);

            //assert
            assertThat(coupon.getIssuedCount()).isEqualTo(1L);
            assertThat(result).isEqualTo(issued);

            verify(couponRepository).findCouponByIdForUpdate(couponId);

        }

        @Test
        @DisplayName("쿠폰이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
        void throwNotFound_whenCouponNotFound() {
            Long couponId = 1L;
            Long userId = 2L;

            when(couponRepository.findCouponByIdForUpdate(couponId))
                    .thenReturn(Optional.empty());
            CouponCommand.Issue command = new CouponCommand.Issue(userId, couponId);

            //act&assert
            assertThatThrownBy(() -> couponService.issue(command))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("쿠폰을 사용할 때")
    @Nested
    class Use{
        @Test
        @DisplayName("쿠폰이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
        void throwNotFound_whenCouponNotFound(){

            Long userId = 1L;
            Long issuedCouponId = 10L;
            Long couponId = 100L;
            Long totalAmount = 1000L;

            IssuedCouponModel issuedCoupon = new IssuedCouponModel(couponId, userId);

            when(issuedCouponRepository.getIssuedCouponOfUser(userId, issuedCouponId)).thenReturn(Optional.of(issuedCoupon));
            when(couponRepository.getCoupon(couponId)).thenReturn(Optional.empty());


            // act&assert
            assertThatThrownBy(() -> couponService.useCoupon(userId, issuedCouponId, totalAmount))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("빌행된 쿠폰이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
        void throwNotFound_whenIssuedCouponNotFound(){

            Long userId = 1L;
            Long issuedCouponId = 10L;
            Long couponId = 100L;
            Long totalAmount = 1000L;

            when(issuedCouponRepository.getIssuedCouponOfUser(userId, issuedCouponId)).thenReturn(Optional.empty());

            // act&assert
            assertThatThrownBy(() -> couponService.useCoupon(userId, issuedCouponId, totalAmount))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

        }
    }
}
