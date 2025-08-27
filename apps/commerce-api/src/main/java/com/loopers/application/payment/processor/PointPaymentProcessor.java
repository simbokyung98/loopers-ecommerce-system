package com.loopers.application.payment.processor;


import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.point.PointService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final PointService pointService;
    private final PaymentService paymentService;

    @Override
    public PaymentType supports() {
        return PaymentType.POINT;
    }

    @Override
    @Transactional
    public void process(PaymentCriteria.CreatePayment criteria) {
        PaymentStatus status = PaymentStatus.SUCCEEDED;

        try{
            pointService.spend(criteria.userId(), criteria.amount());
            PaymentCommand.CreatePointPayment command = criteria.toPointCommand(status);
            paymentService.payPoint(command);

        } catch (IllegalStateException e){
            status = PaymentStatus.FAILED;
            PaymentCommand.CreatePointPayment command = criteria.toPointCommand(status);
            paymentService.payPoint(command);


            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

    }
}
