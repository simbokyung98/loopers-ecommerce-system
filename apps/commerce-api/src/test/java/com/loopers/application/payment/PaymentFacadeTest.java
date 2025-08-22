package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.application.payment.processor.PaymentProcessorFactory;
import com.loopers.domain.payment.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @InjectMocks
    PaymentFacade sut; // System Under Test

    @Mock
    PaymentProcessorFactory factory;

    @Mock
    PaymentProcessor cardProcessor;

    @Mock
    PaymentProcessor pointProcessor;

    @Test
    @DisplayName("type=CARD → Card processor의 process만 호출된다")
    void routes_card() {

        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);

        doReturn(PaymentType.CARD).when(criteria).type();

        when(factory.get(PaymentType.CARD)).thenReturn(cardProcessor);


        sut.pay(criteria);


        verify(factory).get(PaymentType.CARD);
        verify(cardProcessor).process(criteria);
        verify(pointProcessor, never()).process(any());
        verifyNoMoreInteractions(factory, cardProcessor, pointProcessor);
    }

    @Test
    @DisplayName("type=POINT → Point processor의 process만 호출된다")
    void routes_point() {

        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        doReturn(PaymentType.POINT).when(criteria).type();

        when(factory.get(PaymentType.POINT)).thenReturn(pointProcessor);


        sut.pay(criteria);


        verify(factory).get(PaymentType.POINT);
        verify(pointProcessor).process(criteria);
        verify(cardProcessor, never()).process(any());
        verifyNoMoreInteractions(factory, cardProcessor, pointProcessor);
    }
}
