package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<OrderModel> findOrdersByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public List<OrderItemModel> findOrderItemsByOrderId(Long orderId) {
        return orderItemJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<OrderModel> findOrderById(Long id) {
        return orderJpaRepository.findById(id);
    }
}
