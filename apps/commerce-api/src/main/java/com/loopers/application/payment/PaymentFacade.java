package com.loopers.application.payment;


import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.application.payment.processor.PaymentProcessorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentFacade {
    private final PaymentProcessorFactory factory;

    public void pay(PaymentCriteria.CreatePayment criteria){
        PaymentProcessor processor = factory.get(criteria.type());
        processor.process(criteria);
    }
}
