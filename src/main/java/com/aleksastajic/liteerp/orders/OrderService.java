package com.aleksastajic.liteerp.orders;

import com.aleksastajic.liteerp.common.money.MoneyUtil;
import com.aleksastajic.liteerp.inventory.InventoryMovement;
import com.aleksastajic.liteerp.inventory.InventoryMovementRepository;
import com.aleksastajic.liteerp.inventory.InventoryService;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateItemRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderResponse;
import com.aleksastajic.liteerp.products.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final InventoryMovementRepository inventoryMovementRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            InventoryService inventoryService,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        // Basic FK safety: fail fast if any productId doesn't exist (avoid DB constraint 500)
        Set<UUID> productIds = new HashSet<>();
        for (OrderCreateItemRequest item : request.items()) {
            productIds.add(item.productId());
        }
        if (!productIds.isEmpty() && productRepository.countByIdIn(productIds) != productIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "one or more productId values do not exist");
        }

        // Deadlock-safe locking order: sort UUIDs and lock product rows in that order.
        List<UUID> sortedProductIds = new ArrayList<>(new TreeSet<>(productIds));
        if (!sortedProductIds.isEmpty()) {
            productRepository.lockByIdsForUpdate(sortedProductIds);
        }

        Map<UUID, Long> currentStock = inventoryService.getStockByProductIds(sortedProductIds);
        java.util.Map<UUID, Long> delta = new java.util.HashMap<>();
        for (OrderCreateItemRequest item : request.items()) {
            delta.merge(item.productId(), (long) -item.qty(), Long::sum);
        }
        for (UUID productId : sortedProductIds) {
            long next = currentStock.getOrDefault(productId, 0L) + delta.getOrDefault(productId, 0L);
            if (next < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "insufficient stock for productId=" + productId);
            }
        }

        Order order = new Order();
        order.setCustomerRef(request.customerRef());
        order.setStatus(OrderStatus.CREATED);

        for (OrderCreateItemRequest itemReq : request.items()) {
            BigDecimal normalizedUnitPrice;
            try {
                normalizedUnitPrice = MoneyUtil.parseAndNormalize(itemReq.unitPrice());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }

            OrderItem item = new OrderItem();
            item.setProductId(itemReq.productId());
            item.setQty(itemReq.qty());
            item.setUnitPrice(normalizedUnitPrice);
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);

        List<InventoryMovement> movements = new ArrayList<>(request.items().size());
        for (OrderItem item : saved.getItems()) {
            InventoryMovement movement = new InventoryMovement();
            movement.setProductId(item.getProductId());
            movement.setQty(-item.getQty());
            movement.setReason("order");
            movement.setReferenceId(saved.getId());
            movements.add(movement);
        }
        inventoryMovementRepository.saveAll(movements);

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return OrderResponse.from(order);
    }
}
