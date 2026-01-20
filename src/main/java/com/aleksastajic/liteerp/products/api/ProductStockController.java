package com.aleksastajic.liteerp.products.api;

import com.aleksastajic.liteerp.inventory.InventoryService;
import com.aleksastajic.liteerp.products.ProductRepository;
import com.aleksastajic.liteerp.products.api.dto.ProductStockResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductStockController {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public ProductStockController(ProductRepository productRepository, InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{id}/stock")
    public ProductStockResponse getStock(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        return new ProductStockResponse(id, inventoryService.getStock(id));
    }
}
