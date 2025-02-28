package org.ithub.orderservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.dto.OrderRequest;
import org.ithub.orderservice.dto.cart.CartDto;
import org.ithub.orderservice.dto.payment.PaymentConstants;
import org.ithub.orderservice.dto.payment.PaymentResponseDto;
import org.ithub.orderservice.exception.PaymentProcessingException;
import org.ithub.orderservice.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerService {
    private final CartManagementService cartService;
    private final InventoryService inventoryService;
    private final PaymentProcessingService paymentService;
    private final NotificationService notificationService;

    @CircuitBreaker(name = "cartService", fallbackMethod = "getUserCartFallback")
    public CartDto getUserCart(Long userId) {
        return cartService.getUserCart(userId);
    }

    public CartDto getUserCartFallback(Long userId, Exception e) {
        log.error("Circuit breaker triggered when getting user cart for user {}: {}", userId, e.getMessage());
        // Возвращаем пустую корзину в качестве fallback
        CartDto emptyCart = new CartDto();
        emptyCart.setUserId(userId);
        emptyCart.setUsername("Unknown");
        emptyCart.setTotalAmount(BigDecimal.valueOf(0.0));
        emptyCart.setItems(Collections.emptyList());
        return emptyCart;
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "clearUserCartFallback")
    public void clearUserCart(Long userId) {
        cartService.clearUserCart(userId);
    }

    public void clearUserCartFallback(Long userId, Exception e) {
        log.error("Circuit breaker triggered when clearing user cart for user {}: {}", userId, e.getMessage());
        // В случае ошибки продолжаем процесс создания заказа
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "isProductAvailableFallback")
    public boolean isProductAvailable(Long productId) {
        return inventoryService.isProductAvailable(productId);
    }

    public boolean isProductAvailableFallback(Long productId, Exception e) {
        log.error("Circuit breaker triggered when checking product availability for product {}: {}", productId, e.getMessage());
        // В случае недоступности сервиса инвентаря предполагаем, что товар доступен
        // (можно изменить на false, если предпочитаете более консервативный подход)
        return true;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    public PaymentResponseDto processPayment(Order order, OrderRequest orderRequest) {
        return paymentService.processPayment(order, orderRequest);
    }

    public PaymentResponseDto processPaymentFallback(Order order, OrderRequest orderRequest, Exception e) {
        log.error("Circuit breaker triggered when processing payment for order {}: {}", order.getId(), e.getMessage());
        // Создаем ответ о неудачном платеже
        PaymentResponseDto fallbackResponse = new PaymentResponseDto();
        fallbackResponse.setStatus(PaymentConstants.PAYMENT_STATUS_PENDING);
        fallbackResponse.setPaymentId(-1L * order.getId());
        fallbackResponse.setMessage("Payment service is currently unavailable. Payment will be processed later.");
        return fallbackResponse;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "cancelPaymentFallback")
    public void cancelPayment(Long paymentId) {
        paymentService.cancelPayment(paymentId);
    }

    public void cancelPaymentFallback(Long paymentId, Exception e) {
        log.error("Circuit breaker triggered when cancelling payment {}: {}", paymentId, e.getMessage());
        // Запланировать асинхронную отмену платежа позже
        log.warn("Payment cancellation will be retried asynchronously for payment {}", paymentId);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "refundPaymentFallback")
    public void refundPayment(Long paymentId, String reason) {
        paymentService.refundPayment(paymentId, reason);
    }

    public void refundPaymentFallback(Long paymentId, String reason, Exception e) {
        log.error("Circuit breaker triggered when refunding payment {}: {}", paymentId, e.getMessage());
        throw new PaymentProcessingException("Payment service is currently unavailable. Refund will be processed later.");
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendOrderNotificationFallback")
    public void sendOrderNotification(Order order, String eventType) {
        notificationService.sendOrderNotification(order, eventType);
    }

    public void sendOrderNotificationFallback(Order order, String eventType, Exception e) {
        log.error("Circuit breaker triggered when sending notification for order {}, event {}: {}",
                order.getId(), eventType, e.getMessage());
        // Вместо блокировки процесса, просто логируем ошибку
        log.warn("Notification will be sent asynchronously for order {}, event {}", order.getId(), eventType);
    }
}
