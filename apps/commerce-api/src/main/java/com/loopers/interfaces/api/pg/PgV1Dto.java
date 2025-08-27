package com.loopers.interfaces.api.pg;


public class PgV1Dto {

    public record PgCallback(
            String transactionKey,
            String userId,
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String reason

    ){}
}
