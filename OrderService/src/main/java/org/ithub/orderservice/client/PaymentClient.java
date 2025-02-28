package org.ithub.orderservice.client;

import org.ithub.orderservice.dto.payment.PaymentRequestDto;
import org.ithub.orderservice.dto.payment.PaymentResponseDto;
import org.ithub.orderservice.dto.payment.RefundRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentClient {

    @PostMapping("/api/payments")
    ResponseEntity<PaymentResponseDto> processPayment(@RequestBody PaymentRequestDto paymentRequest);

    @GetMapping("/api/payments/{paymentId}")
    ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId);

    @GetMapping("/api/payments/order/{orderId}")
    ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable Long orderId);

    @PostMapping("/api/payments/refund")
    ResponseEntity<PaymentResponseDto> refundPayment(@RequestBody RefundRequestDto refundRequest);

    @DeleteMapping("/api/payments/{paymentId}")
    ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable Long paymentId);
}
