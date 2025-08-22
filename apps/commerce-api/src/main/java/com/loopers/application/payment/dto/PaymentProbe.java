package com.loopers.application.payment.dto;

public record PaymentProbe(Decision decision) {
    public enum Decision { CONFIRMED, RETRY, STOP }
}
