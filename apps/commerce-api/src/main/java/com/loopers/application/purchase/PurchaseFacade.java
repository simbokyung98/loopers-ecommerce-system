package com.loopers.application.purchase;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PurchaseFacade {
    private final PaymentFacade paymentFacade;
    private final OrderFacade orderFacade;
    private final UserService userService;
    private final PaymentService paymentService;

    private final OrderService orderService;
    private final ApplicationEventPublisher events;

    public OrderInfo.OrderResponse purchase(PurchaseCriteria.Purchase criteria){

        userService.checkExistUser(criteria.userId());

        //주문요청
        OrderInfo.OrderResponse order = orderFacade.order(criteria.toOrder());

        try{
            //결제 요청
            paymentFacade.pay(criteria.toPayment(order.orderId(), order.finalAmount()));
            orderFacade.completePayment(order.orderId());
        }catch (RuntimeException ex){
            // 동기 보상: 즉시 취소
            orderFacade.failedPayment(order.orderId());
        }

        return orderFacade.getOrder(order.orderId());

    }

}
