package org.ithub.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.orderservice.model.OrderStatus;
import org.ithub.orderservice.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private PaymentMethod paymentMethod;
    private String trackingNumber;
    private List<OrderItemDto> items = new ArrayList<>();
    private String notes;
    private LocalDateTime lastUpdated;
    private Long paymentId; // Добавленное поле для ссылки на платеж
}