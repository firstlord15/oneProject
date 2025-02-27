package org.ithub.paymentservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ithub.paymentservice.dto.PaymentRequest;
import org.ithub.paymentservice.dto.PaymentResponse;
import org.ithub.paymentservice.dto.RefundRequest;
import org.ithub.paymentservice.exception.PaymentException;
import org.ithub.paymentservice.exception.PaymentNotFoundException;
import org.ithub.paymentservice.model.Payment;
import org.ithub.paymentservice.model.PaymentStatus;
import org.ithub.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentSimulatorService paymentSimulatorService;

    public PaymentService(PaymentRepository paymentRepository, PaymentSimulatorService paymentSimulatorService) {
        this.paymentRepository = paymentRepository;
        this.paymentSimulatorService = paymentSimulatorService;
    }

    // Обрабатывает платежный запрос
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order: {}", paymentRequest.getOrderId());

        Payment payment = Payment.builder()
                .orderId(paymentRequest.getOrderId())
                .amount(paymentRequest.getAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .build();

        payment = paymentRepository.save(payment);

        // Симулируем обработку платежа
        boolean paymentSuccessful = paymentSimulatorService.simulatePaymentProcessing(paymentRequest);

        // Генерируем уникальный transaction ID
        String transactionId = generateTransactionId();
        payment.setTransactionId(transactionId);

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            log.info("Payment successful for order: {}, transaction ID: {}",
                    paymentRequest.getOrderId(), transactionId);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment failed for order: {}", paymentRequest.getOrderId());
        }

        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment,
                paymentSuccessful ? "Payment processed successfully" : "Payment processing failed");
    }

    // Получает статус платежа по его ID
    public PaymentResponse getPaymentStatus(Long paymentId) {
        log.info("Getting payment status for payment ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        return mapToPaymentResponse(payment, "Payment status retrieved successfully");
    }

    // Получает все платежи для заказа
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        log.info("Getting payments for order ID: {}", orderId);
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(payment -> mapToPaymentResponse(payment, "Payment retrieved successfully"))
                .toList();
    }

    // Обрабатывает запрос на возврат средств
    @Transactional
    public PaymentResponse refundPayment(RefundRequest refundRequest) {
        log.info("Processing refund for payment ID: {}", refundRequest.getPaymentId());
        Payment payment = paymentRepository.findById(refundRequest.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + refundRequest.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund payment that is not in COMPLETED state");
        }

        // Симулируем процесс возврата
        boolean refundSuccessful = paymentSimulatorService.simulateRefundProcessing(payment);
        if (refundSuccessful) {
            payment.setStatus(PaymentStatus.REFUNDED);
            log.info("Refund successful for payment ID: {}", refundRequest.getPaymentId());
        } else {
            log.warn("Refund failed for payment ID: {}", refundRequest.getPaymentId());
            throw new PaymentException("Refund processing failed");
        }

        payment = paymentRepository.save(payment);
        return mapToPaymentResponse(payment, "Payment refunded successfully");
    }

    // Отменяет платеж, если он находится в статусе PENDING
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        log.info("Cancelling payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Cannot cancel payment that is not in PENDING state");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment, "Payment cancelled successfully");
    }
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .timestamp(payment.getUpdatedAt())
                .message(message)
                .build();
    }


}
