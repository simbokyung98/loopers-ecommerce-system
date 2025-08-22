package com.loopers.infrastructure.repository.order;

import com.loopers.domain.order.OrderItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemModel, Long> {

    List<OrderItemModel> findByOrderId(Long orderId);
}
