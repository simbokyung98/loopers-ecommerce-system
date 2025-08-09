package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IssuedCouponRepositoryImpl implements IssuedCouponRepository {

    private final IssuedCouponJpaRepository issuedCouponJpaRepository;

    @Override
    public IssuedCouponModel saveIssuedCoupon(IssuedCouponModel issuedCouponModel) {
        return issuedCouponJpaRepository.save(issuedCouponModel);
    }

    @Override
    public Boolean isCouponIssuedToUser(Long userId, Long couponId) {
        return issuedCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public Optional<IssuedCouponModel> getIssuedCouponOfUser(Long userId, Long couponId) {
        return issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public Optional<IssuedCouponModel> getIssuedCoupon(Long id) {
        return issuedCouponJpaRepository.findById(id);
    }
}
