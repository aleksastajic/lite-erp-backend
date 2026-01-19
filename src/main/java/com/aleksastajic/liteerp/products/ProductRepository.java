package com.aleksastajic.liteerp.products;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    long countByIdIn(Collection<UUID> ids);
}
