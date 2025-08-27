package com.loopers.domain.order;


import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="tb_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderModel extends BaseEntity {
    private static final String PATTERN_PHONE_NUMBER = "^01[0-9]\\d{7,8}$";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "issue_coupon_id")
    private Long issueCouponId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "final_amount", nullable = false)
    private Long finalAmount;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;



    public OrderModel(Long userId,
                      Long issueCouponId,
                      Long totalAmount,
                      Long finalAmount,
                      String address,
                      String phoneNumber,
                      String name
                      ) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 아이디 없이 주문을 생성할 수 없습니다.");
        }
        if (totalAmount == null || totalAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 금액은 비어있거나 음수일 수 없습니다.");
        }
        if (finalAmount == null || finalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최종 결제 금액은 비어있을 수 없고 음수일 수 없습니다.");
        }
        if (finalAmount > totalAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최종 결제 금액은 총 금액보다 클 수 없습니다.");
        }
        if (address == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주소 없이 주문을 생성할 수 없습니다.");
        }
        if (phoneNumber == null || !phoneNumber.matches(PATTERN_PHONE_NUMBER)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "핸드폰 번호의 형식과 다릅니다.");
        }
        if (name == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름 없이 주문을 생성할 수 없습니다.");
        }

        this.userId = userId;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.issueCouponId = issueCouponId;

        // 주문 최초 상태
        this.status = OrderStatus.PENDING;
    }

    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제를 완료할 수 없는 주문 상태입니다.");
        }
        this.status = OrderStatus.PAID;
    }

    public void markAsPaymentFailed() {
        if (this.status != OrderStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 실패로 변경할 수 없는 주문 상태입니다.");
        }
        this.status = OrderStatus.PAYMENT_FAILED;
    }
}
