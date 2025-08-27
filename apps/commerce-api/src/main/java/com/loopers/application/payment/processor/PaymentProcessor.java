package com.loopers.application.payment.processor;


import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.payment.PaymentType;

public interface PaymentProcessor {
    PaymentType supports();
    void process(PaymentCriteria.CreatePayment criteria);

}
