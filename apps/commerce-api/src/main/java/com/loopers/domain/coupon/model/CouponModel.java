package com.loopers.domain.coupon.model;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name = "tb_coupon")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discount_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CouponModel extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "issued_count", nullable = false)
    private Long issuedCount = 0L;

    @Column(name = "issued_limit", nullable = false)
    private Long issuedLimit;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;


    protected CouponModel(String name, ZonedDateTime expiresAt, Long issuedLimit) {
        this.name = name;
        this.expiresAt = expiresAt;
        this.issuedLimit = issuedLimit;
    }


    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isIssuable() {
        return issuedCount < issuedLimit;
    }

    public void validateNotExpired() {
        if (isExpired()) {
            throw new CoreException(ErrorType.CONFLICT, "쿠폰 유효기간이 지났습니다.");
        }
    }

    public void issue() {
        validateNotExpired();
        if (!isIssuable()) {
            throw new CoreException(ErrorType.CONFLICT,"쿠폰 발행 수량을 초과했습니다.");
        }
        this.issuedCount++;
    }

    public abstract long calculateDiscount(long orderAmount);

    @Override
    protected void guard() {
        if (issuedLimit <= 0) {
            throw new IllegalArgumentException("쿠폰 설정이 올바르지 않습니다.");
        }
    }

}
