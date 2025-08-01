package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public OrderModel saveOrder(OrderModel orderModel) {
        return orderJpaRepository.save(orderModel);
    }

    @Override
    public void saveOrderItems(List<OrderItemModel> orderItemModels) {
        orderItemJpaRepository.saveAll(orderItemModels);
    }
}
