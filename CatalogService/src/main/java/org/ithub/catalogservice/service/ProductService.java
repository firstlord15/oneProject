package org.ithub.catalogservice.service;

import lombok.extern.slf4j.Slf4j;
import org.ithub.catalogservice.model.Product;
import org.ithub.catalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public long count() {
        return productRepository.count();
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(long id) {
        log.warn("finding product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product updateById(long id, Product newProduct) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(newProduct.getName());
        product.setPrice(newProduct.getPrice());
        product.setAvailable(newProduct.getAvailable());
        product.setDescription(newProduct.getDescription() == null ? "" : newProduct.getDescription());
        log.warn("Updated product: {}", product);

        return productRepository.save(product);
    }

    public void deleteById(long id) {
        log.warn("deleted product by id: {}", id);
        productRepository.deleteById(id);
    }

    public Product save(Product product) {
        log.warn("created product: {}", product);
        return productRepository.save(product);
    }

    public List<Product> search(String keyword) {
        log.warn("finding products by keyword: {}", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> saveAll(List<Product> productList){
        log.warn("created products: {}", productList);
        return productRepository.saveAll(productList);
    }
}
