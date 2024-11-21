package com.shop.generic.orderservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shop.generic.common.clock.GsClock;
import com.shop.generic.common.dtos.OrderCreationDTO;
import com.shop.generic.common.dtos.OrderStatusDTO;
import com.shop.generic.common.dtos.PurchaseProductDTO;
import com.shop.generic.common.entities.Order;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.orderservice.dtos.OrderDetailsDTO;
import com.shop.generic.orderservice.exceptions.OrderNotValidException;
import com.shop.generic.orderservice.repositories.OrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShippingService shippingService;

    @Mock
    private GsClock clock;

    //Should really use constructor injection but showing this as an alternative example for injecting the mocks
    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Given a valid order creation request, when creating a shipping order, then should save the order and create a shipping request")
    void testCreateShippingOrder() {
        // Given
        final PurchaseProductDTO product1 = new PurchaseProductDTO(1, 5, new BigDecimal("10.00"));
        final PurchaseProductDTO product2 = new PurchaseProductDTO(2, 2, new BigDecimal("14.99"));
        final OrderCreationDTO orderCreationDTO = new OrderCreationDTO(List.of(product1, product2),
                "London");
        when(this.clock.getClock()).thenReturn(Clock.fixed(Instant.now(), ZoneId.systemDefault()));

        // When
        final OrderStatusDTO orderStatusDTO = orderService.createShippingOrder(
                orderCreationDTO);

        // Then
        assertNotNull(orderStatusDTO);
        assertEquals(OrderStatus.CREATED, orderStatusDTO.status());

        // Capture the saved order
        final ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        final Order savedOrder = orderCaptor.getValue();

        assertNotNull(savedOrder.getOrderId());
        assertEquals(new BigDecimal("79.98"), savedOrder.getPrice());
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertEquals("1,2", savedOrder.getProductIds());

        verify(orderRepository).save(any(Order.class));
        verify(shippingService).createOrderShippingRequest(any(Order.class));
    }

    @Test
    @DisplayName("Given a null order creation request, when creating a shipping order, then should throw a NullPointerException")
    void testCreateShippingOrder_NullOrderCreationRequest() {
        // Given
        final OrderCreationDTO orderCreationDTO = null;

        //Then
        assertThrows(
                NullPointerException.class,
                () -> orderService.createShippingOrder(orderCreationDTO));
    }

    @Test
    @DisplayName("Given an order creation request with no products, when creating a shipping order, then should throw a RuntimeException")
    void testCreateShippingOrder_EmptyPurchaseProducts() {
        // Given
        final OrderCreationDTO orderCreationDTO = new OrderCreationDTO(Collections.emptyList(),
                "London");

        // When
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () ->
                        orderService.createShippingOrder(orderCreationDTO));

        // Then
        assertEquals("An order cannot be created with no products",
                exception.getMessage());
    }

    @Test
    @DisplayName("Verify a shipping request is not sent if the order fails to persist to the database")
    void testCreateShippingOrder_FailureInRepository() {
        // Given
        final PurchaseProductDTO product1 = new PurchaseProductDTO(1, 2, new BigDecimal("10.00"));
        final OrderCreationDTO orderCreationDTO = new OrderCreationDTO(List.of(product1), "London");
        when(this.clock.getClock()).thenReturn(Clock.fixed(Instant.now(), ZoneId.systemDefault()));

        doThrow(new RuntimeException("Database error")).when(orderRepository)
                .save(any(Order.class));

        final OrderService orderService = new OrderService(orderRepository, shippingService, clock);

        // When
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createShippingOrder(orderCreationDTO);
        });

        assertEquals("Database error", exception.getMessage());

        // Then verify that shipping request was not made due to the exception
        verify(shippingService, never()).createOrderShippingRequest(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid order ID, when fetching order details, then should return OrderDetailsDTO")
    void testFetchOrderDetails_ValidOrderId() {
        // Arrange
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order();
        order.setOrderId(orderId);
        order.setPrice(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.CREATED);
        order.setProductIds("1,2");
        order.setCity("London");
        order.setCreationDate(LocalDateTime.now());

        // Mock the repository to return the order
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        // Act
        final OrderDetailsDTO orderDetailsDTO = orderService.fetchOrderDetails(orderId);

        // Assert
        assertNotNull(orderDetailsDTO);
        assertEquals(orderId, orderDetailsDTO.orderId());
        assertEquals(order.getPrice(), orderDetailsDTO.price());
        assertEquals(order.getStatus(), orderDetailsDTO.status());
        assertEquals(order.getProductIds(), orderDetailsDTO.productIds());
        assertEquals(order.getCity(), orderDetailsDTO.city());
    }

    @Test
    @DisplayName("Given an invalid order ID, when fetching order details, then should throw OrderNotFoundException")
    void testFetchOrderDetails_InvalidOrderId() {
        // Given
        final UUID invalidOrderId = UUID.randomUUID();
        when(orderRepository.findByOrderId(invalidOrderId)).thenReturn(Optional.empty());

        // When/Then
        final OrderNotValidException exception = assertThrows(OrderNotValidException.class,
                () -> orderService.fetchOrderDetails(invalidOrderId));
        assertEquals(String.format("Order with id %s not found", invalidOrderId),
                exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid order ID and status, when updating order, then should update status and return OrderStatusDTO")
    void testUpdateOrder_ValidOrderId() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());
        order.setLastUpdated(LocalDateTime.now());

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(this.clock.getClock()).thenReturn(Clock.fixed(Instant.now(), ZoneId.systemDefault()));

        // When
        final OrderStatusDTO result = orderService.updateOrder(orderId, OrderStatus.SHIPPED);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.orderId());
        assertEquals(OrderStatus.SHIPPED, result.status());

        verify(orderRepository, times(1)).save(order);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    @DisplayName("Given an invalid order ID, when updating order, then should throw RuntimeException")
    void testUpdateOrder_InvalidOrderId() {
        // Given
        final UUID invalidOrderId = UUID.randomUUID();
        when(orderRepository.findByOrderId(invalidOrderId)).thenReturn(Optional.empty());

        // When
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrder(invalidOrderId, OrderStatus.SHIPPED));

        // Then
        assertEquals(String.format("Order with id %s not found", invalidOrderId),
                exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
}