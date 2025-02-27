package org.ithub.orderservice.model;

public enum OrderStatus {
    CREATED,        // Заказ создан
    PENDING,        // Ожидает оплаты
    PAID,           // Оплачен
    PROCESSING,     // В обработке
    SHIPPING,       // Отправлен
    DELIVERED,      // Доставлен
    COMPLETED,      // Выполнен
    CANCELLED,      // Отменен
    REFUNDED        // Возвращен
}
