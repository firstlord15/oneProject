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

    // Уменьшает количество товара на складе
    public void reduceStock(Long productId, int quantity) {
        try {
            log.info("Reducing stock for product ID: {}, quantity: {}", productId, quantity);
            catalogClient.updateStock(productId, -quantity);
            log.info("Stock reduced successfully for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to reduce stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to update stock: " + e.getMessage());
        }
    }

    // Увеличивает количество товара на складе
    public void increaseStock(Long productId, int quantity) {
        try {
            log.info("Increasing stock for product ID: {}, quantity: {}", productId, quantity);
            catalogClient.updateStock(productId, quantity);
            log.info("Stock increased successfully for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to increase stock for product {}: {}", productId, e.getMessage());
            // Логируем ошибку, но не прерываем выполнение, так как это может быть не критично
        }
    }
}