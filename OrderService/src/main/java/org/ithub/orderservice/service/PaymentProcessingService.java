package org.ithub.orderservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.client.PaymentClient;
import org.ithub.orderservice.dto.OrderRequest;
import org.ithub.orderservice.dto.payment.PaymentConstants;
import org.ithub.orderservice.dto.payment.PaymentRequestDto;
import org.ithub.orderservice.dto.payment.PaymentResponseDto;
import org.ithub.orderservice.dto.payment.RefundRequestDto;
import org.ithub.orderservice.exception.PaymentProcessingException;
import org.ithub.orderservice.model.Order;
import org.ithub.orderservice.model.PaymentMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentClient paymentClient;

    // Метод для обработки платежа
    public PaymentResponseDto processPayment(Order order, OrderRequest orderRequest) {
        log.info("Processing payment for order: {}", order.getId());

        // Конвертируем метод оплаты из внутреннего перечисления в строковое представление для платежного сервиса
        String paymentMethod = mapToPaymentServiceMethod(order.getPaymentMethod());

        // Создаем запрос на платеж
        PaymentRequestDto paymentRequest = PaymentRequestDto.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .build();

        // Добавляем детали платежа в зависимости от метода оплаты
        if (PaymentConstants.PAYMENT_METHOD_CREDIT_CARD.equals(paymentMethod) ||
                PaymentConstants.PAYMENT_METHOD_DEBIT_CARD.equals(paymentMethod)) {
            paymentRequest.setCardNumber(orderRequest.getCardNumber());
            paymentRequest.setCardHolderName(orderRequest.getCardHolderName());
            paymentRequest.setExpiryDate(orderRequest.getExpiryDate());
            paymentRequest.setCvv(orderRequest.getCvv());
        } else if (PaymentConstants.PAYMENT_METHOD_ELECTRONIC_WALLET.equals(paymentMethod)) {
            paymentRequest.setWalletId(orderRequest.getWalletId());
        }

        try {
            // Отправляем запрос на обработку платежа
            PaymentResponseDto response = paymentClient.processPayment(paymentRequest).getBody();

            if (response == null) {
                throw new PaymentProcessingException("Payment service returned null response");
            }

            return response;
        } catch (FeignException e) {
            log.error("Payment service error: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to process payment: " + e.getMessage());
        }
    }

    // Отменяет платеж
    public void cancelPayment(Long paymentId) {
        try {
            log.info("Cancelling payment with ID: {}", paymentId);
            paymentClient.cancelPayment(paymentId);
            log.info("Payment cancelled successfully: {}", paymentId);
        } catch (Exception e) {
            log.error("Failed to cancel payment {}: {}", paymentId, e.getMessage());
            throw new PaymentProcessingException("Failed to cancel payment: " + e.getMessage());
        }
    }

    // Выполняет возврат платежа
    public PaymentResponseDto refundPayment(Long paymentId, String reason) {
        try {
            log.info("Processing refund for payment ID: {}, reason: {}", paymentId, reason);

            RefundRequestDto refundRequest = new RefundRequestDto();
            refundRequest.setPaymentId(paymentId);
            refundRequest.setReason(reason);

            PaymentResponseDto refundResponse = paymentClient.refundPayment(refundRequest).getBody();

            if (refundResponse == null) {
                throw new PaymentProcessingException("Payment service returned null response for refund");
            }

            if (!PaymentConstants.PAYMENT_STATUS_REFUNDED.equals(refundResponse.getStatus())) {
                throw new PaymentProcessingException("Refund failed. Payment status: " + refundResponse.getStatus());
            }

            log.info("Refund processed successfully for payment ID: {}", paymentId);
            return refundResponse;

        } catch (FeignException e) {
            log.error("Payment service error during refund: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
        }
    }

    // Получает статус платежа
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        try {
            log.info("Getting payment status for ID: {}", paymentId);
            PaymentResponseDto response = paymentClient.getPaymentStatus(paymentId).getBody();

            if (response == null) {
                throw new PaymentProcessingException("Payment service returned null response");
            }

            return response;
        } catch (FeignException e) {
            log.error("Payment service error getting status: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to get payment status: " + e.getMessage());
        }
    }

    // Конвертирует метод оплаты из внутреннего перечисления в строковое представление для платежного сервиса
    private String mapToPaymentServiceMethod(PaymentMethod method) {
        return switch (method) {
            case CARD -> PaymentConstants.PAYMENT_METHOD_CREDIT_CARD;
            case FPS -> PaymentConstants.PAYMENT_METHOD_ELECTRONIC_WALLET;
            case CASH -> PaymentConstants.PAYMENT_METHOD_CASH_ON_DELIVERY;
        };
    }

}
