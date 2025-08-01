package com.loopers.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    OrderModel saveOrder(OrderModel orderMode);
    void saveOrderItems(List<OrderItemModel> orderItemModels);

    List<OrderModel> findOrdersByUserId(Long userId);

    List<OrderItemModel> findOrderItemsByOrderId(Long orderId);

    Optional<OrderModel> findOrderById(Long id);

}
