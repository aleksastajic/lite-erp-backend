package com.aleksastajic.liteerp.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    interface StockRow {
        UUID getProductId();

        Long getQty();
    }

    @Query(
            value = "select product_id as productId, coalesce(sum(qty), 0) as qty " +
                    "from inventory_movements " +
                    "where product_id in (:productIds) " +
                    "group by product_id",
            nativeQuery = true
    )
    List<StockRow> sumQtyByProductIds(@Param("productIds") Collection<UUID> productIds);
}
