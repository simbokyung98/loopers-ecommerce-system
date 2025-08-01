package com.loopers.domain.order;

import java.util.List;

public interface OrderRepository {

    OrderModel saveOrder(OrderModel orderMode);
    void saveOrderItems(List<OrderItemModel> orderItemModels);

}
