package org.ithub.orderservice.service;


import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.exception.InvalidOrderStateException;
import org.ithub.orderservice.model.OrderStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderStatusService {

    // Валидирует возможность перехода из одного статуса в другой
    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // Тот же статус, ничего не делаем
        }

        log.debug("Validating status transition from {} to {}", currentStatus, newStatus);

        // Проверяем допустимые переходы
        switch (currentStatus) {
            case CREATED -> {
                if (newStatus != OrderStatus.PENDING && newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.PAID) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case PENDING -> {
                if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case PAID -> {
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.REFUNDED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case PROCESSING -> {
                if (newStatus != OrderStatus.SHIPPING && newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.REFUNDED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case SHIPPING -> {
                if (newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.REFUNDED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case DELIVERED -> {
                if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.REFUNDED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case COMPLETED, CANCELLED -> {
                if (newStatus != OrderStatus.REFUNDED) {
                    throw new InvalidOrderStateException(
                            "Cannot change status from " + currentStatus + " to " + newStatus);
                }
            }
            case REFUNDED ->
                    throw new InvalidOrderStateException("Cannot change status from REFUNDED to any other status");
            default -> throw new InvalidOrderStateException("Unknown order status: " + currentStatus);
        }

        log.debug("Status transition from {} to {} is valid", currentStatus, newStatus);
    }

    // Проверяет, можно ли отменить заказ в текущем статусе
    public boolean canBeCancelled(OrderStatus status) {
        return status != OrderStatus.SHIPPING &&
                status != OrderStatus.DELIVERED &&
                status != OrderStatus.COMPLETED &&
                status != OrderStatus.REFUNDED;
    }
}