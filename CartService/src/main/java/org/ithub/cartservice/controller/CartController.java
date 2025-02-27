package org.ithub.cartservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.cartservice.dto.CartDto;
import org.ithub.cartservice.exception.CartNotFoundException;
import org.ithub.cartservice.exception.ProductNotFoundException;
import org.ithub.cartservice.exception.UserNotFoundException;
import org.ithub.cartservice.response.CartItemResponse;
import org.ithub.cartservice.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Controller", description = "API для управления корзиной покупателя")
public class CartController {
    private final CartService cartService;

    @GetMapping("/{cartId}")
    @Operation(summary = "Получить корзину по ID")
    public ResponseEntity<CartDto> getCartById(@PathVariable Long cartId) {
        log.info("Request to get cart with id: {}", cartId);
        CartDto cartDto = cartService.getCartById(cartId);
        return ResponseEntity.ok(cartDto);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить корзину пользователя по ID пользователя")
    public ResponseEntity<CartDto> getCartByUserId(@PathVariable Long userId) {
        log.info("Request to get cart for user with id: {}", userId);
        CartDto cartDto = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/user/add/{userId}")
    @Operation(summary = "Добавить товар в корзину")
    public ResponseEntity<CartDto> addProductToCart(@PathVariable Long userId, @RequestBody CartItemResponse item) {
        log.info("Request to add product {} to cart for user {} with quantity {}", item.getProductId(), userId, item.getQuantity());
        CartDto cartDto = cartService.addProductToCart(userId, item.getProductId(), item.getQuantity());
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/user/update/{userId}")
    @Operation(summary = "Обновить количество товара в корзине")
    public ResponseEntity<CartDto> updateProductQuantity(@PathVariable Long userId, @RequestBody CartItemResponse item) {
        log.info("Request to update quantity of product {} to {} in cart for user {}", item.getProductId(), item.getQuantity(), userId);
        CartDto cartDto = cartService.updateProductQuantity(userId, item.getProductId(), item.getQuantity());
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/user/{userId}/products/{productId}")
    @Operation(summary = "Удалить товар из корзины")
    public ResponseEntity<CartDto> removeProductFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        log.info("Request to remove product {} from cart for user {}", productId, userId);
        CartDto cartDto = cartService.removeProductFromCart(userId, productId);
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Очистить корзину пользователя")
    public ResponseEntity<CartDto> clearCart(@PathVariable Long userId) {
        log.info("Request to clear cart for user {}", userId);
        CartDto cartDto = cartService.clearCart(userId);
        return ResponseEntity.ok(cartDto);
    }

    @ExceptionHandler({CartNotFoundException.class, UserNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequestException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}