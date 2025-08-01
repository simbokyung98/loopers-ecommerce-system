package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;

import java.util.List;

public class OrderAmountCalculator {
    private OrderAmountCalculator() {} // 생성 금지

    public static long calculateTotalAmount(List<OrderCommand.Product> products) {
        return products.stream()
                .mapToLong(p -> p.price() * p.quantity())
                .sum();
    }
}
