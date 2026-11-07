package com.example.engine.payment;

/** Port/interface: business logic depends on this contract, not a vendor SDK. */
public interface PaymentGateway {
    GatewayResult charge(String idempotencyKey, long amountInMinorUnits);
    record GatewayResult(boolean successful, String reference, String failureReason) {}
}
