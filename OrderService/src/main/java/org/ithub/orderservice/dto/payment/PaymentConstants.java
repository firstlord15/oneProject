package org.ithub.orderservice.dto.payment;

public class PaymentConstants {
    // Статусы платежей
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";

    // Методы оплаты для отправки в PaymentService
    public static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYMENT_METHOD_DEBIT_CARD = "DEBIT_CARD";
    public static final String PAYMENT_METHOD_ELECTRONIC_WALLET = "ELECTRONIC_WALLET";
    public static final String PAYMENT_METHOD_CASH_ON_DELIVERY = "CASH_ON_DELIVERY";
}
