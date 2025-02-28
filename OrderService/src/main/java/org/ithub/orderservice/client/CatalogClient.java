package org.ithub.orderservice.client;

import org.ithub.orderservice.dto.catalog.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("api/catalog/{id}")
    ProductDto getProductById(@PathVariable("id") Long productId);

    @PutMapping("api/catalog/{id}")
    void updateStock(@PathVariable("id") Long productId);
}
