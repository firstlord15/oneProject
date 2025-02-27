package org.ithub.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.dto.OrderDto;
import org.ithub.orderservice.dto.OrderRequest;
import org.ithub.orderservice.dto.OrderStatusUpdateRequest;
import org.ithub.orderservice.exception.InvalidOrderStateException;
import org.ithub.orderservice.exception.OrderNotFoundException;
import org.ithub.orderservice.exception.ProductNotFoundException;
import org.ithub.orderservice.model.OrderStatus;
import org.ithub.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Controller", description = "API для управления заказами")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Создать новый заказ")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("REST request to create order for user: {}", orderRequest.getUserId());
        OrderDto createdOrder = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Получить заказ по ID")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
        log.info("REST request to get order with ID: {}", orderId);
        OrderDto orderDto = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить все заказы пользователя")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        log.info("REST request to get all orders for user: {}", userId);
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получить заказы по статусу с пагинацией")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sort) {

        log.info("REST request to get orders with status: {}, page: {}, size: {}", status, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<OrderDto> orders = orderService.getOrdersByStatus(status, pageRequest);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Обновить статус заказа")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest statusUpdateRequest) {

        log.info("REST request to update status for order {}: {}", orderId, statusUpdateRequest.getStatus());
        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, statusUpdateRequest);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Отменить заказ")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {

        log.info("REST request to cancel order with ID: {}", orderId);
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date-range")
    @Operation(summary = "Получить заказы за период времени")
    public ResponseEntity<List<OrderDto>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("REST request to get orders between {} and {}", startDate, endDate);
        List<OrderDto> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @ExceptionHandler({OrderNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({InvalidOrderStateException.class, IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequestException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
