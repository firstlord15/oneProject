package org.ithub.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.dto.OrderDto;
import org.ithub.orderservice.dto.OrderItemDto;
import org.ithub.orderservice.dto.OrderRequest;
import org.ithub.orderservice.dto.OrderStatusUpdateRequest;
import org.ithub.orderservice.dto.cart.CartDto;
import org.ithub.orderservice.dto.payment.PaymentConstants;
import org.ithub.orderservice.dto.payment.PaymentResponseDto;
import org.ithub.orderservice.exception.InvalidOrderStateException;
import org.ithub.orderservice.exception.OrderNotFoundException;
import org.ithub.orderservice.exception.PaymentProcessingException;
import org.ithub.orderservice.model.Order;
import org.ithub.orderservice.model.OrderItem;
import org.ithub.orderservice.model.OrderStatus;
import org.ithub.orderservice.model.PaymentMethod;
import org.ithub.orderservice.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentProcessingService paymentService;
    private final OrderStatusService statusService;
    private final CircuitBreakerService circuitBreakerService;

    @Transactional
    public OrderDto createOrder(OrderRequest orderRequest) {
        log.info("Creating new order for user: {}", orderRequest.getUserId());

        // Получаем корзину пользователя с применением Circuit Breaker
        CartDto cart = circuitBreakerService.getUserCart(orderRequest.getUserId());

        // Создаем новый заказ
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setUsername(cart.getUsername());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setBillingAddress(orderRequest.getBillingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setNotes(orderRequest.getNotes());
        order.setTotalAmount(cart.getTotalAmount());

        // Добавляем товары в заказ и проверяем их наличие
        addItemsToOrder(order, cart);

        // Сохраняем заказ
        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID: {}", savedOrder.getId());

        // Очищаем корзину пользователя с применением Circuit Breaker
        circuitBreakerService.clearUserCart(orderRequest.getUserId());

        // Обрабатываем платеж если не наличными
        processOrderPayment(savedOrder, orderRequest);

        // Отправляем уведомление с применением Circuit Breaker
        circuitBreakerService.sendOrderNotification(savedOrder, "ORDER_CREATED");

        return convertToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = findOrderById(orderId);
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.info("Fetching orders with status: {}", status);
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        return orderPage.map(this::convertToDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatusUpdateRequest updateRequest) {
        log.info("Updating status for order {}: {}", orderId, updateRequest.getStatus());

        Order order = findOrderById(orderId);

        // Проверяем валидность перехода статуса
        statusService.validateStatusTransition(order.getStatus(), updateRequest.getStatus());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(updateRequest.getStatus());

        // Обновляем примечания к заказу если есть
        updateOrderNotes(order, updateRequest.getNotes());

        // Обновляем номер отслеживания если есть
        if (updateRequest.getTrackingNumber() != null && !updateRequest.getTrackingNumber().isEmpty()) {
            order.setTrackingNumber(updateRequest.getTrackingNumber());
        }

        // Обработка возврата денег
        if (updateRequest.getStatus() == OrderStatus.REFUNDED && order.getPaymentId() != null) {
            processRefund(order, updateRequest.getNotes());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Updated order status from {} to {}", oldStatus, savedOrder.getStatus());

        // Отправляем уведомление с применением Circuit Breaker
        circuitBreakerService.sendOrderNotification(savedOrder, "STATUS_UPDATED");

        return convertToDto(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = findOrderById(orderId);

        // Проверяем, можно ли отменить заказ
        if (!statusService.canBeCancelled(order.getStatus())) {
            throw new InvalidOrderStateException("Cannot cancel order in status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        // Добавляем причину отмены
        updateOrderNotes(order, reason != null ? "Cancel reason: " + reason : null);

        // Отменяем платеж с применением Circuit Breaker
        if (order.getPaymentId() != null) {
            circuitBreakerService.cancelPayment(order.getPaymentId());
        }

        orderRepository.save(order);
        log.info("Cancelled order ID: {} (previous status: {})", orderId, oldStatus);

        // Возвращаем товары на склад с применением Circuit Breaker
        for (OrderItem item : order.getItems()) {
            circuitBreakerService.increaseStock(item.getProductId(), item.getQuantity());
        }

        // Отправляем уведомление с применением Circuit Breaker
        circuitBreakerService.sendOrderNotification(order, "ORDER_CANCELLED");
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    private void addItemsToOrder(Order order, CartDto cart) {
        for (var cartItem : cart.getItems()) {
            // Проверяем наличие товара
            if (!inventoryService.isProductAvailable(cartItem.getProductId())) {
                throw new IllegalStateException("Product is not available: " + cartItem.getProductName());
            }

            // Добавляем товар в заказ
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.addItem(orderItem);

            // Уменьшаем остаток на складе
            inventoryService.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }
    }

    private void processOrderPayment(Order order, OrderRequest orderRequest) {
        // Инициируем платеж, если это не оплата при получении
        if (orderRequest.getPaymentMethod() != PaymentMethod.CASH) {
            try {
                PaymentResponseDto paymentResponse = paymentService.processPayment(order, orderRequest);

                // Если платеж успешно обработан, обновляем статус заказа
                if (PaymentConstants.PAYMENT_STATUS_COMPLETED.equals(paymentResponse.getStatus())) {
                    order.setStatus(OrderStatus.PAID);
                    log.info("Payment completed for order: {}, transaction ID: {}",
                            order.getId(), paymentResponse.getTransactionId());
                } else {
                    // Если оплата не прошла, оставляем заказ в статусе CREATED
                    log.warn("Payment not completed for order: {}, status: {}",
                            order.getId(), paymentResponse.getStatus());
                }

                // Сохраняем ID платежа в заказе для будущих ссылок
                order.setPaymentId(paymentResponse.getPaymentId());
                orderRepository.save(order);

            } catch (Exception e) {
                log.error("Error processing payment for order {}: {}", order.getId(), e.getMessage());
                // Оставляем заказ в статусе CREATED, платеж можно будет повторить позже
            }
        } else {
            // Для оплаты наличными при доставке сразу переводим заказ в статус PENDING
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
        }
    }

    private void processRefund(Order order, String reason) {
        try {
            paymentService.refundPayment(order.getPaymentId(), reason);
            log.info("Payment refunded for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error processing refund for order {}: {}", order.getId(), e.getMessage());
            throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
        }
    }

    private void updateOrderNotes(Order order, String newNotes) {
        if (newNotes != null && !newNotes.isEmpty()) {
            order.setNotes(order.getNotes() != null ? order.getNotes() + "\n" + newNotes : newNotes);
        }
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setUsername(order.getUsername());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setNotes(order.getNotes());
        dto.setLastUpdated(order.getLastUpdated());
        dto.setPaymentId(order.getPaymentId());

        dto.setItems(order.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private OrderItemDto convertToDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}