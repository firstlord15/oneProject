package org.ithub.cartservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.cartservice.dto.CartDto;
import org.ithub.cartservice.dto.CartItemDto;
import org.ithub.cartservice.dto.ProductDto;
import org.ithub.cartservice.dto.UserDto;
import org.ithub.cartservice.exception.CartNotFoundException;
import org.ithub.cartservice.model.Cart;
import org.ithub.cartservice.model.CartItem;
import org.ithub.cartservice.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartCircuitBreakerService circuitBreakerService;

    @Transactional(readOnly = true)
    public CartDto getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return convertToDto(cart);
    }

    @Transactional
    public CartDto addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // Получаем информацию о продукте через Circuit Breaker
        ProductDto productDto = circuitBreakerService.getProductById(productId);

        if (!productDto.isAvailable()) {
            throw new IllegalStateException("Product is not available: " + productId);
        }

        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = new CartItem();
        cartItem.setProductId(productDto.getId());
        cartItem.setProductName(productDto.getName());
        cartItem.setPrice(productDto.getPrice());
        cartItem.setQuantity(quantity);
        cartItem.setProductImageUrl(null);
        cart.addItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        log.info("Added product {} to cart for user {}", productId, userId);
        return convertToDto(savedCart);
    }

    @Transactional
    public CartDto updateProductQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeProductFromCart(userId, productId);
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        cart.updateItemQuantity(productId, quantity);
        Cart savedCart = cartRepository.save(cart);
        log.info("Updated quantity for product {} to {} in cart for user {}", productId, quantity, userId);
        return convertToDto(savedCart);
    }

    @Transactional
    public CartDto removeProductFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        cart.removeItem(productId);
        Cart savedCart = cartRepository.save(cart);
        log.info("Removed product {} from cart for user {}", productId, userId);
        return convertToDto(savedCart);
    }

    @Transactional
    public CartDto clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        cart.clearCart();
        Cart savedCart = cartRepository.save(cart);
        log.info("Cleared cart for user {}", userId);
        return convertToDto(savedCart);
    }

    @Transactional(readOnly = true)
    public CartDto getCartById(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));
        return convertToDto(cart);
    }

    // Получает существующую корзину пользователя или создает новую
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            // Получаем информацию о пользователе через Circuit Breaker
            UserDto userDto = circuitBreakerService.getUserById(userId);

            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setUsername(userDto.getUsername());
            return cartRepository.save(newCart);
        });
    }

    // Конвертирует сущность Cart в DTO
    private CartDto convertToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setUserId(cart.getUserId());
        cartDto.setUsername(cart.getUsername());
        cartDto.setTotalAmount(cart.getTotalAmount());
        cartDto.setItems(cart.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
        return cartDto;
    }

    // Конвертирует сущность CartItem в DTO
    private CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setId(cartItem.getId());
        cartItemDto.setProductId(cartItem.getProductId());
        cartItemDto.setProductName(cartItem.getProductName());
        cartItemDto.setPrice(cartItem.getPrice());
        cartItemDto.setQuantity(cartItem.getQuantity());
        cartItemDto.setProductImageUrl(cartItem.getProductImageUrl());
        cartItemDto.setSubtotal(cartItem.getSubtotal());
        return cartItemDto;
    }
}
