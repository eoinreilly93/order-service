package com.shop.generic.orderservice.dtos;

import com.shop.generic.common.entities.Order;
import com.shop.generic.common.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDetailsDTO(UUID orderId, BigDecimal price, String productIds, OrderStatus status,
                              String city, LocalDateTime creationDate) {

    public OrderDetailsDTO(final Order order) {
        this(order.getOrderId(), order.getPrice(), order.getProductIds(),
                order.getStatus(), order.getCity(), order.getCreationDate());
    }
}
