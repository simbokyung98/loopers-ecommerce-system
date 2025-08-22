package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentGatewayService;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.scheduler.PaymentFollowUpScheduler;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;

    private final PaymentFollowUpScheduler followUpScheduler;

    @Override
    public PaymentType supports() {
        return PaymentType.CARD;
    }

    @Override
    public void process(PaymentCriteria.CreatePayment criteria) {

        String transactionKey = paymentGatewayService.requestWithStatusRecovery(criteria.toRequest());
        if (transactionKey == null || transactionKey.isBlank()) {
            // 정상적으로는 도달하지 않음(실패 시 PG 서비스가 예외 던짐). 방어 로직.
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 트랜잭션 키가 비어있습니다.");
        }

        PaymentCommand.CreateCardPayment command = criteria.toCommand(transactionKey);
        PaymentModel paymentModel =  paymentService.payCardPending(command);

        followUpScheduler.scheduleEveryMinute(criteria.orderId(), paymentModel.getId(),transactionKey, 30);
    }
}
