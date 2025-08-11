package com.loopers.domain.coupon.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@DiscriminatorValue("PERCENTAGE")
@Getter
@NoArgsConstructor
public class PercentageCouponModel extends CouponModel {

    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    public PercentageCouponModel(String name,  Long issuedLimit, ZonedDateTime expiresAt, Long discountValue) {
        super(name, expiresAt, issuedLimit);

        if (discountValue == null || discountValue <= 0 || discountValue > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율은 1 이상 100 이하의 값이어야 합니다.");
        }


        this.discountValue = discountValue;
    }


    @Override
    public long calculateDiscount(long orderAmount) {
        if (isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 만료되었습니다.");
        }
        if (orderAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 금액은 0보다 커야 합니다.");
        }
        long discount = orderAmount * discountValue / 100;
        return Math.min(discount, orderAmount);
    }
}
