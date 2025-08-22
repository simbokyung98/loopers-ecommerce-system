package com.loopers.domain.payment;


import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="tb_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentModel extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "pg_tx_id")
    private String pgTxId;

    /** ✅ 1) 기존 생성자: 임의 타입 + 즉시 성공 케이스 (유지) */
    public PaymentModel(Long orderId, Long userId, PaymentType type, Long amount) {
        validateCommon(orderId, userId, type, amount);
        this.orderId = orderId;
        this.userId  = userId;        // ← 누락되어 있던 userId 세팅 추가
        this.type    = type;
        this.amount  = amount;
        this.status  = PaymentStatus.SUCCEEDED;
    }


    private PaymentModel(Long orderId, Long userId, PaymentType type,
                         Long amount, PaymentStatus status, String pgTxId) {
        validateCommon(orderId, userId, type, amount);
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제상태는 비어있을 수 없습니다.");
        }
        if (type == PaymentType.CARD && (pgTxId == null || pgTxId.isBlank())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 결제에서는 트랜잭션키가 필요합니다.");
        }

        this.orderId = orderId;
        this.userId  = userId;
        this.type    = type;
        this.amount  = amount;
        this.status  = status;
        this.pgTxId  = pgTxId;
    }


    public static PaymentModel point(Long orderId, Long userId, Long amount, PaymentStatus status) {
        return new PaymentModel(orderId, userId, PaymentType.POINT, amount, status, null);
    }


    public static PaymentModel cardPending(Long orderId, Long userId, Long amount, String pgTxId) {
        return new PaymentModel(orderId, userId, PaymentType.CARD, amount, PaymentStatus.PENDING, pgTxId);
    }

    public void success(){
        if(this.status == PaymentStatus.SUCCEEDED){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 처리된 요청입니다.");
        }

        this.status = PaymentStatus.SUCCEEDED;
    }

    public void failed(){

        if(this.status == PaymentStatus.FAILED){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 처리된 요청입니다.");
        }

        this.status = PaymentStatus.FAILED;
    }



    private static void validateCommon(Long orderId, Long userId, PaymentType type, Long amount) {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문ID는 비어있을 수 없습니다.");
        }
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저ID는 비어있을 수 없습니다.");
        }
        if (type == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제타입은 비어있을 수 없습니다.");
        }
        if (amount == null || amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제금액은 양의 정수이어야 합니다.");
        }
    }
}
