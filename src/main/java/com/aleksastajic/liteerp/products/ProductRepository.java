package com.aleksastajic.liteerp.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    long countByIdIn(Collection<UUID> ids);

    @Query(value = "select id from products where id in (:ids) order by id for update", nativeQuery = true)
    List<UUID> lockByIdsForUpdate(@Param("ids") Collection<UUID> ids);
}
