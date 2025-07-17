package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name ="point")
@Getter
public class PointModel extends BaseEntity {

    @Column(unique = true)
    private Long userId;
    private Long point;

    public PointModel() {}


    public PointModel(Long userId, Long point){
        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저ID는 비어있을 수 없습니다.");
        }
        if(point == null || point <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 비어있거나 0보다 작을 수 없습니다.");
        }
        this.userId = userId;
        this.point = point;
    }

    public void charge(Long newPoint){
        if(newPoint <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "충전금액은 0 보다 커야합니다.");

        this.point += newPoint;
    }


}
