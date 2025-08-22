package com.loopers.infrastructure.repository.payment;


import com.loopers.domain.payment.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentModel, Long> {
}
