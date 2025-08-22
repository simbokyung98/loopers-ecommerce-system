package com.loopers.infrastructure.repository.coupon;

import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<CouponModel> findCouponByIdForUpdate(Long couponId) {

        return couponJpaRepository.findCouponByIdForUpdate(couponId);
    }

    @Override
    public CouponModel saveCoupon(CouponModel couponModel) {
        return couponJpaRepository.save(couponModel);
    }

    @Override
    public Optional<CouponModel> getCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }
}
