package org.ithub.cartservice.client;

import org.ithub.cartservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("api/catalog/{id}")
    ProductDto getProductById(@PathVariable("id") Long productId);
}