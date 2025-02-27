package org.ithub.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.ithub.paymentservice.dto.PaymentRequest;
import org.ithub.paymentservice.model.Payment;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class PaymentSimulatorService {

    private final Random random = new Random();

    // Симулирует обработку платежа внешней платежной системой (с вероятностью 90%)
    public boolean simulatePaymentProcessing(PaymentRequest paymentRequest) {
        log.info("Simulating payment processing for order: {}", paymentRequest.getOrderId());

        // Имитация задержки при обработке платежа
        simulateProcessingDelay();

        // Валидация платежных данных (в зависимости от метода оплаты)
        boolean isValid = validatePaymentData(paymentRequest);
        if (!isValid) {
            log.warn("Payment data validation failed for order: {}", paymentRequest.getOrderId());
            return false;
        }

        // Имитация успешной обработки платежа с вероятностью 90%
        boolean isSuccessful = random.nextDouble() < 0.9;
        log.info("Payment processing simulation result for order {}: {}",
                paymentRequest.getOrderId(), isSuccessful ? "SUCCESSFUL" : "FAILED");

        return isSuccessful;
    }

    // Симулирует обработку возврата платежа (с вероятностью 95%)
    public boolean simulateRefundProcessing(Payment payment) {
        log.info("Simulating refund processing for payment: {}", payment.getId());

        // Имитация задержки при обработке возврата
        simulateProcessingDelay();

        // Имитация успешного возврата с вероятностью 95%
        boolean isSuccessful = random.nextDouble() < 0.95;
        log.info("Refund processing simulation result for payment {}: {}",
                payment.getId(), isSuccessful ? "SUCCESSFUL" : "FAILED");

        return isSuccessful;
    }

    // Имитирует задержку при обработке запроса
    private void simulateProcessingDelay() {
        // Имитация задержки от 500 до 2000 мс
        int delay = 500 + random.nextInt(1500);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Валидирует данные платежа в зависимости от метода оплаты
    private boolean validatePaymentData(PaymentRequest paymentRequest) {
        return switch (paymentRequest.getPaymentMethod()) {
            case CREDIT_CARD, DEBIT_CARD -> validateCardData(paymentRequest);
            case BANK_TRANSFER -> validateBankTransferData(paymentRequest);
            case ELECTRONIC_WALLET -> validateWalletData(paymentRequest);
            case CASH_ON_DELIVERY -> true; // Всегда валидно
            default -> false;
        };
    }

    private boolean validateCardData(PaymentRequest request) {
        return request.getCardNumber() != null && !request.getCardNumber().isEmpty() &&
                request.getCardHolderName() != null && !request.getCardHolderName().isEmpty() &&
                request.getExpiryDate() != null && !request.getExpiryDate().isEmpty() &&
                request.getCvv() != null && !request.getCvv().isEmpty();
    }

    private boolean validateBankTransferData(PaymentRequest request) {
        return request.getAccountNumber() != null && !request.getAccountNumber().isEmpty() &&
                request.getBankCode() != null && !request.getBankCode().isEmpty();
    }

    private boolean validateWalletData(PaymentRequest request) {
        return request.getWalletId() != null && !request.getWalletId().isEmpty();
    }
}
