package com.loopers.application.payment.processor;

import com.loopers.domain.payment.PaymentType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProcessorFactory {

    private final Map<PaymentType, PaymentProcessor> registry;

    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        this.registry = new EnumMap<>(PaymentType.class);
        processors.forEach(p -> registry.put(p.supports(), p));
    }

    public PaymentProcessor get(PaymentType type) {
        PaymentProcessor processor = registry.get(type);
        if (processor == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 결제 방식: " + type);
        }
        return processor;
    }
}
