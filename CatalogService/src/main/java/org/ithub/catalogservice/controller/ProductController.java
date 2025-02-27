package org.ithub.catalogservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.catalogservice.model.Product;
import org.ithub.catalogservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по ID")
    public ResponseEntity<Product> findProductById(@PathVariable Long id){
        log.info("Request to get product with id: {}", id);
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить товар")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("Request to update product with id: {}", id);
        return ResponseEntity.ok(productService.updateById(id, product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        log.info("Request to delete product with id: {}", id);
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск товаров по ключевому слову")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        log.info("Request to search products with keyword: {}", keyword);
        return ResponseEntity.ok(productService.search(keyword));
    }

    @GetMapping("/all")
    @Operation(summary = "Получить все товары")
    public ResponseEntity<List<Product>> findAllProduct(){
        log.info("Request to get all products");
        return ResponseEntity.ok(productService.findAll());
    }

    @PostMapping
    @Operation(summary = "Создать новый товар")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("Request to create a new product: {}", product.getName());
        return new ResponseEntity<>(productService.save(product), HttpStatus.CREATED);
    }

    @GetMapping("/createTest")
    @Operation(summary = "Создать тестовые товары")
    public ResponseEntity<String> createTestProducts() {
        log.info("Request to create test products");
        if (productService.count() > 0) {
            return ResponseEntity.ok("Таблица уже содержит данные.");
        }

        List<Product> testProducts = List.of(
                new Product("PC", "PC desc", new BigDecimal(100000), true),
                new Product("Laptop", "Laptop desc", new BigDecimal(150000), true),
                new Product("Phone", "Phone desc", new BigDecimal(70000), false),
                new Product("Mouse", "Mouse desc", new BigDecimal(5000), true),
                new Product("Keyboard", "Keyboard desc", new BigDecimal(13000), false),
                new Product("Monitor", "Monitor desc", new BigDecimal(35000), true)
        );

        productService.saveAll(testProducts);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Тестовые продукты созданы успешно.");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequestException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
