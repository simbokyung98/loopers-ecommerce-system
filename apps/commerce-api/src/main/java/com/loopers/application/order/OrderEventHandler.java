package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent e) {
        OrderResult.Order order = orderService.getOrderDetailById(e.orderId());
        //이벤트 처리
        if(order.issueCouponId() != null){
            couponService.restoreCoupon(order.issueCouponId());
        }

        order.orderItems().forEach(item ->
                productService.restoreStock(ProductCommand.ProductQuantity.of(item.productId(), item.quantity()))
        );

    }

}
