package com.loopers.interfaces.api.pg;

import com.loopers.application.payment.scheduler.PaymentFollowUpUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pg")
@Slf4j
@RequiredArgsConstructor
public class PgCallbackV1Controller {

    private final PaymentFollowUpUseCase paymentFollowUpUseCase;

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody PgV1Dto.PgCallback payload) {
        // 콜백 들어오는 데이터 확인용 로그
        log.info("💳 [PG-CALLBACK] txKey={}, userId={}, orderId={}, cardType={}, cardNo={}, amount={}, status={}, reason={}",
                 payload.transactionKey(),  payload.userId(),  payload.orderId(),
                 payload.cardType(),  payload.cardNo(),  payload.amount(),
                 payload.status(),  payload.reason());

        paymentFollowUpUseCase.onCardPaymentCallback(Long.valueOf(payload.orderId()), payload.transactionKey());
        return ResponseEntity.ok().build();
    }
}
