package com.loopers.infrastructure.repository.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;


    @Override
    public PaymentModel savePayment(PaymentModel paymentModel) {
        return paymentJpaRepository.save(paymentModel);
    }

    @Override
    public Optional<PaymentModel> getPayment(Long id) {
        return paymentJpaRepository.findById(id);
    }
}
