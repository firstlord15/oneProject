package org.ithub.orderservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;

    // Поля для разных методов оплаты
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    private String accountNumber;
    private String bankCode;

    private String walletId;
}