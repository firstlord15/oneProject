package org.ithub.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.orderservice.model.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Billing address is required")
    private String billingAddress;

    @NotBlank(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String notes;

    // Поля для платежных данных
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    private String accountNumber;
    private String bankCode;

    private String walletId;
}