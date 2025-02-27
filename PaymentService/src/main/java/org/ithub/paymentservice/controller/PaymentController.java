package org.ithub.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.paymentservice.dto.PaymentRequest;
import org.ithub.paymentservice.dto.PaymentResponse;
import org.ithub.paymentservice.dto.RefundRequest;
import org.ithub.paymentservice.exception.PaymentException;
import org.ithub.paymentservice.exception.PaymentNotFoundException;
import org.ithub.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment Controller", description = "API для управления платежами")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Обработать платеж")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("Request to process payment for order: {}", paymentRequest.getOrderId());
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Получить статус платежа")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long paymentId) {
        log.info("Request to get payment status for payment ID: {}", paymentId);
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Получить платежи по заказу")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable Long orderId) {
        log.info("Request to get payments for order ID: {}", orderId);
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/refund")
    @Operation(summary = "Выполнить возврат платежа")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundRequest refundRequest) {
        log.info("Request to refund payment with ID: {}", refundRequest.getPaymentId());
        PaymentResponse response = paymentService.refundPayment(refundRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Отменить платеж")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId) {
        log.info("Request to cancel payment with ID: {}", paymentId);
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler({PaymentNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({PaymentException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequestException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
