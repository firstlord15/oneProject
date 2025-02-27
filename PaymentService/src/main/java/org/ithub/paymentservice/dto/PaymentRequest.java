package org.ithub.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.ithub.paymentservice.model.PaymentMethod;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Для кредитной карты (заглушка)
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    // Для банковского перевода
    private String accountNumber;
    private String bankCode;

    // Для электронного кошелька
    private String walletId;
}