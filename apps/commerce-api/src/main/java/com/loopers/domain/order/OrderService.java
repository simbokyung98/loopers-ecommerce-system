package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void placeOrder(OrderCommand.PlaceOrder command){

        OrderModel orderModel = new OrderModel(
                command.userId(),
                command.amount(),
                command.address(),
                command.phoneNumber(),
                command.name()
        );

        OrderModel order = orderRepository.saveOrder(orderModel);

        List<OrderItemModel> orderItemModels = command.products()
                        .stream().map(product -> new OrderItemModel(
                                order.getId(), product.productId(),product.name(), product.price(), product.quantity()
                )).toList();

        orderRepository.saveOrderItems(orderItemModels);
        
    }
}
