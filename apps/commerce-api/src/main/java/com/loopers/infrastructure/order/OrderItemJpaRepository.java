package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemModel, Long> {
}
