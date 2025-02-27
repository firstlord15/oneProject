package org.ithub.orderservice.client;

import org.ithub.orderservice.dto.cart.CartDto;
import org.ithub.orderservice.dto.cart.CartItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "cart-service", url = "${cart.service.url}")
public interface CartClient {

    @GetMapping("api/carts/user/{userId}/items")
    List<CartItemResponse> getCartItems(@PathVariable("userId") Long userId);

    @GetMapping("api/carts/user/{userId}")
    CartDto getCartByUserId(@PathVariable("userId") Long userId);

    @DeleteMapping("api/carts/user/{userId}")
    void clearCart(@PathVariable("userId") Long userId);
}