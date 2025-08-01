package com.loopers.domain.order;

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
@Table(name = "tb_order_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "product_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemModel extends BaseEntity {
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "price", nullable = false)
    private Long price;
    @Column(name = "quantity", nullable = false)
    private Long quantity;
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    public OrderItemModel(Long orderId, Long productId, String name, Long price, Long quantity){
        if(orderId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문아이디 없이 주문아이템을 생성할 수 없습니다.");
        }

        if(productId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품아이디 없이 주문아이템을 생성할 수 없습니다.");
        }

        if(name == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름 없이 주문아이템을 생성할 수 없습니다.");
        }

        if(price == null || price < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격 비어있거나 음수일 수 없습니다.");
        }

        if(quantity == null || quantity <0){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 개수는 비어있거나 음수일 수 없습니다.");
        }

        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.status = OrderStatus.PAID;
    }
}
