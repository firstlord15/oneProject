package org.ithub.cartservice.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {
    private long productId;
    private int quantity;
}
