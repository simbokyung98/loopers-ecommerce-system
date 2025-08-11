package com.loopers.application.order.dto;


import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.ProductCommand;

import java.util.List;

public class OrderCriteria {

    public record Order(
            Long userId,
            Long issueCouponId,
            String address,
            String phoneNumber,
            String name,
            List<ProductQuantity> productQuantities

    ){
        public OrderCommand.PlaceOrder toCommand(Long amount, List<OrderCommand.Product> products){
            return new OrderCommand.PlaceOrder(userId, amount, address, phoneNumber, name, products);
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
