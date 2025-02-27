package org.ithub.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.model.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    // Отправляет уведомление о событии с заказом
    public void sendOrderNotification(Order order, String eventType) {
        Map<String, Object> notificationDetails = new HashMap<>();
        notificationDetails.put("orderId", order.getId());
        notificationDetails.put("userId", order.getUserId());
        notificationDetails.put("username", order.getUsername());
        notificationDetails.put("orderStatus", order.getStatus());
        notificationDetails.put("totalAmount", order.getTotalAmount());
        notificationDetails.put("eventType", eventType);

        // Вместо вызова внешнего сервиса просто логируем информацию
        log.info("Order notification: Type: {}, Order ID: {}, Status: {}",
                eventType, order.getId(), order.getStatus());

        // В реальной системе здесь можно будет подключить NotificationClient,
        // когда он будет доступен
        // notificationClient.sendOrderNotification(notificationDetails);
    }

    // Отправляет уведомление об ошибке
    public void sendErrorNotification(Long orderId, String errorType, String errorMessage) {
        log.error("Order error notification: Order ID: {}, Error type: {}, Message: {}",
                orderId, errorType, errorMessage);

        // В реальной системе здесь можно будет отправлять уведомления администраторам или в систему мониторинга
    }
}