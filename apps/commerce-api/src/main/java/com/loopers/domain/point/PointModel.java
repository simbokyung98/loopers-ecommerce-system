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
@Table(name ="point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointModel extends BaseEntity {

    @Column(unique = true)
    private Long userId;
    private Long amount;
    
    public PointModel(Long userId){
        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저ID는 비어있을 수 없습니다.");
        }

        this.userId = userId;
        this.amount = 0L;
    }

    public void charge(Long newPoint){
        if(newPoint <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "충전금액은 0 보다 커야합니다.");

        this.amount += newPoint;
    }

}
