package com.loopers.application.order.dto;


import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.product.ProductCommand;
import lombok.Builder;

import java.util.List;

public class OrderCriteria {

    @Builder
    public record Order(
            Long userId,
            Long issueCouponId,
            String address,
            String phoneNumber,
            String name,
            PaymentType type,
            String cardType,
            String cardNo,
            List<ProductQuantity> productQuantities

    ){
        public OrderCommand.PlaceOrder toCommand(Long totalAmount, Long finalAmount, List<OrderCommand.Product> products){
            return new OrderCommand.PlaceOrder(userId, issueCouponId, totalAmount,finalAmount, address, phoneNumber, name, products);
        }
        public ProductCommand.DeductStocks toDeductStocks(){
            return new ProductCommand.DeductStocks(productQuantities.stream().map(ProductQuantity::of).toList());
        }
    }

    public record ProductQuantity(
            Long productId,
            Long quantity
    ){
        public ProductCommand.ProductQuantity of() {
            return new ProductCommand.ProductQuantity(productId, quantity);
        }
    }
}
