package com.loopers.domain.order;


import java.util.List;

public class OrderResult {

    public record Order(
            Long id,
            Long userId,
            Long amount,
            String address,
            String phoneNumber,
            String name,
            List<OrderItem> orderItems
    ) {
        public static Order from(OrderModel orderModel, List<OrderItemModel> orderItemModels){

            List<OrderItem> orderItemResponse = orderItemModels.stream()
                    .map(OrderItem::from).toList();

            return new Order(
                    orderModel.getId(),
                    orderModel.getUserId(),
                    orderModel.getAmount(),
                    orderModel.getAddress(),
                    orderModel.getPhoneNumber(),
                    orderModel.getName(),
                    orderItemResponse
            );
        }

    }

    public record OrderItem(
          Long id,
          Long orderId,
          Long productId,
          String name,
          Long price,
          Long quantity,
          OrderStatus status
    ){
        public static OrderItem from(OrderItemModel orderItemModel){
            return new OrderItem(
                    orderItemModel.getId(),
                    orderItemModel.getOrderId(),
                    orderItemModel.getProductId(),
                    orderItemModel.getName(),
                    orderItemModel.getPrice(),
                    orderItemModel.getQuantity(),
                    orderItemModel.getStatus()
            );
        }
    }
}
