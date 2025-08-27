package com.loopers.infrastructure.http.dto;

public record TransactionResponse(
        String transactionKey,
        String status,
        String reason
) {
}
