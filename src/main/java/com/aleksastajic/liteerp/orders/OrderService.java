package com.aleksastajic.liteerp.orders;

import com.aleksastajic.liteerp.common.money.MoneyUtil;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateItemRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderResponse;
import com.aleksastajic.liteerp.products.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
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
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return OrderResponse.from(order);
    }
}
