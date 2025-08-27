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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "issued_coupon",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_id"}))
public class IssuedCouponModel extends BaseEntity {

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issued_at", nullable = false)
    private ZonedDateTime issuedAt;

    @Column(name = "used_at")
    private ZonedDateTime usedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public IssuedCouponModel(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
        this.issuedAt = ZonedDateTime.now();
    }

    public void use() {
        if (this.usedAt != null) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 사용된 쿠폰입니다.");
        }

        this.usedAt = ZonedDateTime.now();
    }

    public void restore() {
        if (this.usedAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아직 사용되지 않은 쿠폰은 복원할 수 없습니다.");
        }
        this.usedAt = null;
    }
}
