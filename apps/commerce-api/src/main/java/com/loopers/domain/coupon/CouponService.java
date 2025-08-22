package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public IssuedCouponModel issue(CouponCommand.Issue command){

        CouponModel couponModel = couponRepository.findCouponByIdForUpdate(command.couponId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용가능한 쿠폰이 존재하지 않습니다"));

        couponModel.issue();

        IssuedCouponModel issuedCouponModel = new IssuedCouponModel(command.couponId(), command.userId());
        return issuedCouponRepository.saveIssuedCoupon(issuedCouponModel);
    }

    public void validateCouponNotIssued(CouponCommand.Issue command){
       Boolean exist = issuedCouponRepository.isCouponIssuedToUser(command.userId(), command.couponId());

       if(exist){
           throw new CoreException(ErrorType.BAD_REQUEST, "이미 발행된 쿠폰입니다.");
       }
    }

    @Transactional
    public Long useCoupon(Long userId, Long issueCouponId, Long orderTotalAmount){
        try{
            IssuedCouponModel issuedCouponModel = issuedCouponRepository.getIssuedCouponOfUser(userId, issueCouponId)
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "발행된 쿠폰이 존재하지 않습니다"));

            CouponModel couponModel = couponRepository.getCoupon(issuedCouponModel.getCouponId())
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정보가 존재하지 않습니다."));

            couponModel.validateNotExpired();
            Long discountAmount = couponModel.calculateDiscount(orderTotalAmount);

            issuedCouponModel.use();

            return discountAmount;


        }catch  (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
    }

    public void restoreCoupon(Long issueCouponId){
        IssuedCouponModel issuedCouponModel = issuedCouponRepository.getIssuedCoupon(issueCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "발행된 쿠폰이 존재하지 않습니다."));

        issuedCouponModel.restore();
    }


}
