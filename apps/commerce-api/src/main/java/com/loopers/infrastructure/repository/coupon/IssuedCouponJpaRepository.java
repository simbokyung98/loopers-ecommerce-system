package com.loopers.infrastructure.repository.coupon;

import com.loopers.domain.coupon.model.IssuedCouponModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssuedCouponJpaRepository extends JpaRepository<IssuedCouponModel, Long> {

    Boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    Optional<IssuedCouponModel> findByUserIdAndCouponId(Long userId, Long couponId);
}
