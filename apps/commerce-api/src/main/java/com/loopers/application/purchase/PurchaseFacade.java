package com.loopers.application.purchase;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PurchaseFacade {
    private final PaymentFacade paymentFacade;
    private final OrderFacade orderFacade;
    private final UserService userService;
    private final PaymentService paymentService;

    public OrderInfo.OrderResponse purchase(PurchaseCriteria.Purchase criteria){

        userService.checkExistUser(criteria.userId());

        //주문요청
        OrderInfo.OrderResponse order = orderFacade.order(criteria.toOrder());

        try{
            //결제 요청
            paymentFacade.pay(criteria.toPayment(order.orderId(), order.finalCount()));
            orderFacade.completePayment(order.orderId());
        }catch (RuntimeException ex){
            // 동기 보상: 즉시 취소
            orderFacade.failPayment(order.orderId());
        }

        return orderFacade.getOrder(order.orderId());

    }

    @Transactional
    public void failedPayment(Long paymentId, Long orderId){
        paymentService.failedPay(paymentId);
        orderFacade.failPayment(orderId);
    }
}
