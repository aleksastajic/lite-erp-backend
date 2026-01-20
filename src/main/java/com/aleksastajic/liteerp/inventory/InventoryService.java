package com.aleksastajic.liteerp.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryService(InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional(readOnly = true)
    public long getStock(UUID productId) {
        return getStockByProductIds(java.util.List.of(productId)).getOrDefault(productId, 0L);
    }

    @Transactional(readOnly = true)
    public Map<UUID, Long> getStockByProductIds(Collection<UUID> productIds) {
        Map<UUID, Long> result = new HashMap<>();
        for (UUID id : productIds) {
            result.put(id, 0L);
        }

        if (productIds.isEmpty()) {
            return result;
        }

        for (InventoryMovementRepository.StockRow row : inventoryMovementRepository.sumQtyByProductIds(productIds)) {
            result.put(row.getProductId(), row.getQty());
        }
        return result;
    }
}
