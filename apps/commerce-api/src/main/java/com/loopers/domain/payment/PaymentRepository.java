package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    PaymentModel savePayment(PaymentModel paymentModel);

    Optional<PaymentModel> getPayment(Long id);
}
