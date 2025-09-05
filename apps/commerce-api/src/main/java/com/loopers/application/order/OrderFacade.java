package com.loopers.application.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentCreatedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderFacade {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher events;

    @Transactional
    public OrderInfo.OrderResponse order(OrderCriteria.Order criteria){

        //유저 체크
        userService.checkExistUser(criteria.userId());

        Map<Long, OrderCriteria.ProductQuantity> productQuantityMap =
               criteria.productQuantities().stream().collect(Collectors.toMap(OrderCriteria.ProductQuantity::productId, Function.identity()));

        //재고 확보 및 주문 스냅샷을 위한 재고 리스트 get(락 걸기)
        List<ProductModel> productModels = productService.getSellableProductsByIdInForUpdate(productQuantityMap.keySet().stream().toList());

        //상품 체크
        if(productQuantityMap.size() != productModels.size()){
            throw new CoreException(ErrorType.NOT_FOUND, "주문할 수 없는 상품이 있습니다.");
        }

        //주문 아이템 생성
        List<OrderCommand.Product> commandProducts = new ArrayList<>();

        for(ProductModel model : productModels){
            OrderCriteria.ProductQuantity productQuantity = productQuantityMap.get(model.getId());

            commandProducts.add(new OrderCommand.Product(
                    model.getId(),
                    model.getName(),
                    model.getPrice(),
                    productQuantity.quantity()
            ));
        }

        //총금액계산
        long totalAmount = orderService.calculateTotalAmount(commandProducts);

        long finalAmount = totalAmount;

        //쿠폰사용
        if(criteria.issueCouponId() != null){
            long discountAmount = couponService.useCoupon(criteria.userId(), criteria.issueCouponId(), totalAmount);

            finalAmount = totalAmount - discountAmount;
        }


        //재고차감
        productService.deductStocks(criteria.toDeductStocks());

        //주문 생성
        OrderCommand.PlaceOrder placeOrder = criteria.toCommand(totalAmount, finalAmount, commandProducts);
        OrderModel orderModel = orderService.placeOrder(placeOrder);

        //주문이벤트 발행
        events.publishEvent(new OrderCreatedEvent(
                orderModel.getId(),
                criteria.userId(),
                finalAmount,
                criteria.type(),
                criteria.cardType(),
                criteria.cardNo()
        ));

        return OrderInfo.OrderResponse.from(orderModel);

    }

    @Transactional(readOnly = true)
    public OrderInfo.OrderDetail getOrder(Long orderId){
        OrderResult.Order order = orderService.getOrderDetailById(orderId);

        return OrderInfo.OrderDetail.from(order);
    }

    @Transactional(readOnly = true)
    public OrderInfo.UserOrders getOrdersByUserId(Long userId){
        //유저 체크
        userService.checkExistUser(userId);

        List<OrderModel> orderModels = orderService.getOrdersByUserId(userId);

        return OrderInfo.UserOrders.from(orderModels);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failedPayment(Long orderId){

        orderService.failPayment(orderId);
        events.publishEvent(new PaymentFailedEvent(orderId));

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completePayment(Long orderId){
        orderService.completePayment(orderId);
        events.publishEvent(new PaymentCreatedEvent(orderId));
    }


}
