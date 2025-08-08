package com.loopers.domain.Like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_like",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeModel extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    Long userId;
    @Column(name = "product_id", nullable = false)
    Long productId;


    public LikeModel(Long userId, Long productId){
        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 아이디 없이 좋아요를 생성할 수 없습니다.");
        }

        if(productId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 아이디 없이 좋아요를 생성할 수 없습니다.");
        }

        this.userId = userId;
        this.productId = productId;
    }


}
