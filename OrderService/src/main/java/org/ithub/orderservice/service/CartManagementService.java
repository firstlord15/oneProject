package org.ithub.orderservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.client.CartClient;
import org.ithub.orderservice.dto.cart.CartDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartManagementService {
    private final CartClient cartClient;

    // Получает корзину пользователя
    public CartDto getUserCart(Long userId) {
        try {
            log.info("Getting cart for user: {}", userId);
            CartDto cart = cartClient.getCartByUserId(userId);

            if (cart.getItems().isEmpty()) {
                log.warn("Cart is empty for user: {}", userId);
                throw new IllegalStateException("Cart is empty for user: " + userId);
            }

            return cart;
        } catch (FeignException e) {
            log.error("Error fetching cart for user {}: {}", userId, e.getMessage());
            throw new IllegalStateException("Failed to retrieve cart: " + e.getMessage());
        }
    }

    // Очищает корзину пользователя
    public void clearUserCart(Long userId) {
        try {
            log.info("Clearing cart for user: {}", userId);
            cartClient.clearCart(userId);
            log.info("Cart cleared successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to clear cart for user {}: {}", userId, e.getMessage());
            // Логируем ошибку, но не прерываем выполнение, так как это может быть не критично
        }
    }
}