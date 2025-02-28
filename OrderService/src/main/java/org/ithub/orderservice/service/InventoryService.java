package org.ithub.orderservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.orderservice.client.CatalogClient;
import org.ithub.orderservice.dto.catalog.ProductDto;
import org.ithub.orderservice.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final CatalogClient catalogClient;

    // Получает информацию о продукте по его ID
    public ProductDto getProductInfo(Long productId) {
        try {
            log.info("Getting product information for ID: {}", productId);
            return catalogClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            log.error("Product not found: {}", productId);
            throw new ProductNotFoundException("Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error retrieving product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to retrieve product information: " + e.getMessage());
        }
    }

    // Проверяет доступность продукта
    public boolean isProductAvailable(Long productId) {
        ProductDto product = getProductInfo(productId);
        return product.isAvailable();
    }
}