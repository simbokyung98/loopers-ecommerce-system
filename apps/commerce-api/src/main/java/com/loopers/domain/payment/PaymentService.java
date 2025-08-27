package com.loopers.domain.payment;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentModel payPoint(PaymentCommand.CreatePointPayment command) {
        PaymentModel model = PaymentModel.point(
                command.orderId(),
                command.userId(),
                command.amount(),
                command.status()
        );
        return paymentRepository.savePayment(model);
    }

    @Transactional
    public PaymentModel payCardPending(PaymentCommand.CreateCardPayment command) {
        PaymentModel model = PaymentModel.cardPending(
                command.orderId(),
                command.userId(),
                command.amount(),
                command.pgTxId()
        );
        return paymentRepository.savePayment(model);
    }

    public void completePay(Long id){
        PaymentModel paymentModel = paymentRepository.getPayment(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제정보가 존재하지 않습니다."));


        paymentModel.success();
    }

    public void failedPay(Long id){
        PaymentModel paymentModel = paymentRepository.getPayment(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제정보가 존재하지 않습니다."));


        paymentModel.failed();
    }



}
