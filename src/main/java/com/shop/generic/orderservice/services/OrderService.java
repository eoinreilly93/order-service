package com.shop.generic.orderservice.services;

import com.shop.generic.common.dtos.OrderCreationDTO;
import com.shop.generic.common.dtos.OrderStatusDTO;
import com.shop.generic.common.dtos.PurchaseProductDTO;
import com.shop.generic.common.entities.Order;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.orderservice.dtos.OrderDetailsDTO;
import com.shop.generic.orderservice.exceptions.OrderNotFoundException;
import com.shop.generic.orderservice.repositories.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShippingService shippingService;

    public OrderService(final OrderRepository orderRepository,
            final ShippingService shippingService) {
        this.orderRepository = orderRepository;
        this.shippingService = shippingService;
    }

    public OrderStatusDTO createShippingOrder(final OrderCreationDTO orderCreationDTO) {
        final UUID orderId = UUID.randomUUID();
        final Order order = createOrder(orderId, orderCreationDTO);
        saveOrder(order);
        createAndSendShippingRequest(order);
        return new OrderStatusDTO(orderId, OrderStatus.CREATED);
    }

    public OrderStatusDTO updateOrder(final UUID orderId, final OrderStatus newStatus) {
        final Optional<Order> order = this.orderRepository.findByOrderId(orderId);

        if (order.isEmpty()) {
            throw new RuntimeException(String.format("Order with id %s not found", orderId));
        }
        order.get().setStatus(newStatus);
        this.orderRepository.save(order.get());
        log.info("Order {} status updated to {}", orderId, newStatus);
        return new OrderStatusDTO(order.get().getOrderId(), order.get().getStatus());
    }

    private Order createOrder(final UUID orderId, final OrderCreationDTO orderCreationDTO) {
        if (orderCreationDTO.purchaseProductDTOS().isEmpty()) {
            throw new OrderNotFoundException("An order cannot be created with no products");
        }
        final BigDecimal orderCost = orderCreationDTO.purchaseProductDTOS().stream()
                .map(product -> product.price().multiply(BigDecimal.valueOf(product.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Order order = new Order();
        order.setPrice(orderCost.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(OrderStatus.CREATED);

        final List<Integer> productIds = orderCreationDTO.purchaseProductDTOS().stream()
                .map(PurchaseProductDTO::productId)
                .toList();

        final String productIdsAsString = productIds.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        order.setProductIds(productIdsAsString);
        order.setOrderId(orderId);
        order.setCity(orderCreationDTO.city());
        order.setCreationDate(LocalDateTime.now());
        return order;
    }

    public OrderDetailsDTO fetchOrderDetails(final UUID orderId) {
        final Optional<Order> order = this.orderRepository.findByOrderId(orderId);
        return new OrderDetailsDTO(order.orElseThrow(() -> new OrderNotFoundException(
                String.format("Order with id %s not found", orderId))));
    }

    private void saveOrder(final Order order) {
        this.orderRepository.save(order);
        log.info("Persisted order {} to the database", order.getOrderId());
    }


    private void createAndSendShippingRequest(final Order order) {
        log.info("Creating shipping request");
        shippingService.createOrderShippingRequest(order);
    }
}
