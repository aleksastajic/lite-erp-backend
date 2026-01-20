package com.aleksastajic.liteerp.orders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    Page<OrderItem> findByOrder_Id(UUID orderId, Pageable pageable);
}
