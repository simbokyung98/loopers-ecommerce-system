package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="tb_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointModel extends BaseEntity {

    @Column(name = "user_id",unique = true, nullable = false)
    private Long userId;
    @Column(name = "amount", nullable = false)
    private Long totalAmount;

    public PointModel(Long userId){
        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저ID는 비어있을 수 없습니다.");
        }

        this.userId = userId;
        this.totalAmount = 0L;
    }

    public void charge(Long amount){
        if(amount <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "충전금액은 0 보다 커야합니다.");

        this.totalAmount += amount;
    }

    public void spand(Long amount){
        if(amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용금액은 0 보다 커야합니다.");
        }

        long spendAmount = this.totalAmount - amount;

        if(spendAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

        this.totalAmount = spendAmount;
    }

}
