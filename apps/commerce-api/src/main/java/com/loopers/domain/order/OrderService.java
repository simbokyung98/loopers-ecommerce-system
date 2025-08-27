package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderModel placeOrder(OrderCommand.PlaceOrder command){

        OrderModel orderModel = new OrderModel(
                command.userId(),
                command.couponId(),
                command.totalAmount(),
                command.finalAmount(),
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

        return order;
    }

    public List<OrderModel> getOrdersByUserId(Long userId){
        return orderRepository.findOrdersByUserId(userId);
    }

    public OrderResult.Order getOrderDetailById(Long orderId){
        OrderModel orderModel = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문정보가 존재하지 않습니다."));

        List<OrderItemModel> orderItemModels = getOrderItemByOrderId(orderModel.getId());

        return OrderResult.Order.from(orderModel, orderItemModels);
    }

    public List<OrderItemModel> getOrderItemByOrderId(Long orderId){
        return orderRepository.findOrderItemsByOrderId(orderId);
    }

    public long calculateTotalAmount(List<OrderCommand.Product> products) {
        return products.stream()
                .mapToLong(p -> p.price() * p.quantity())
                .sum();
    }

    public void completePayment(Long orderId){
        OrderModel orderModel = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문정보가 존재하지 않습니다."));
        orderModel.markAsPaid();

    }

    public  void failPayment(Long orderId){
        OrderModel orderModel = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문정보가 존재하지 않습니다."));
        orderModel.markAsPaymentFailed();
    }

}
