package com.loopers.domain.cart;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_cart",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartModel extends BaseEntity {
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "quantity", nullable = false)
    private Long quantity;
    
    public CartModel(Long productId, Long userId, Long quantity){
        if(productId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 아이디 없이 장바구니를 생성할 수 없습니다.");
        }

        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 아이디 없이 장바구니를 생성할 수 없습니다.");
        }

        if(quantity == null || quantity< 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 주문 개수 없이 장바구니를 생성할 수 없습니다.");
        }

        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
    }
}
