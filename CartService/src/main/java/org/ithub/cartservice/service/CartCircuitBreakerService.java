package org.ithub.cartservice.service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.cartservice.client.CatalogClient;
import org.ithub.cartservice.client.UserClient;
import org.ithub.cartservice.dto.ProductDto;
import org.ithub.cartservice.dto.UserDto;
import org.ithub.cartservice.exception.ProductNotFoundException;
import org.ithub.cartservice.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCircuitBreakerService {
    private final CatalogClient catalogClient;
    private final UserClient userClient;

    // Получение информации о продукте с применением Circuit Breaker
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductByIdFallback")
    public ProductDto getProductById(Long productId) {
        try {
            return catalogClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product with id {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error fetching product data: " + e.getMessage());
        }
    }

    // Fallback метод для получения продукта при срабатывании Circuit Breaker
    public ProductDto getProductByIdFallback(Long productId, Exception e) {
        log.error("Circuit breaker triggered for product id {}: {}", productId, e.getMessage());

        // Создаем базовую информацию о продукте для аварийного режима
        ProductDto fallbackProduct = new ProductDto();
        fallbackProduct.setId(productId);
        fallbackProduct.setName("Временно недоступный товар");
        fallbackProduct.setPrice(BigDecimal.valueOf(0.0));
        fallbackProduct.setAvailable(false);

        return fallbackProduct;
    }

    // Получение информации о пользователе с применением Circuit Breaker
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDto getUserById(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with id: " + userId);
        } catch (Exception e) {
            log.error("Error fetching user with id {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error fetching user data: " + e.getMessage());
        }
    }

    // Fallback метод для получения пользователя при срабатывании Circuit Breaker
    public UserDto getUserByIdFallback(Long userId, Exception e) {
        log.error("Circuit breaker triggered for user id {}: {}", userId, e.getMessage());

        // Создаем базовую информацию о пользователе для аварийного режима
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setUsername("temp_user_" + userId);

        return fallbackUser;
    }
}
