package com.loopers.domain.order;


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
@Table(name ="tb_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderModel extends BaseEntity {
    private static final String PATTERN_PHONE_NUMBER = "^01[0-9]\\d{7,8}$";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private OrderStatus status;

    public OrderModel(Long userId, Long amount, String address, String phoneNumber,
                      String name, OrderStatus staus){
        if(userId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 아이디 없이 주문을 생성할 수 없습니다.");
        }
        if(amount == null || amount <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "총 가격 은 비어있거나 음수일 수 업습니다.");
        }
        if(address == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "주소 없이 주문을 생성할 수 없습니다.");
        }
        if(phoneNumber == null || !phoneNumber.matches(PATTERN_PHONE_NUMBER)){
            throw new CoreException(ErrorType.BAD_REQUEST, "핸드폰 번호의 형식과 다릅니다.");
        }
        if(name == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "이름 없이 주문을 생성할 수 없습니다.");
        }
        if(staus == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "주문상태 없이 주문을 생성할 수 없습니다.");
        }

        this.userId = userId;
        this.amount = amount;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.status = staus;
    }
}
