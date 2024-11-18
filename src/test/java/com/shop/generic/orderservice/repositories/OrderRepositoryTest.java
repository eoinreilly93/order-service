package com.shop.generic.orderservice.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shop.generic.common.entities.Order;
import com.shop.generic.common.entities.OrderAudit;
import com.shop.generic.common.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


class OrderRepositoryTest extends BaseRepositoriesTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Repository should save an order")
    public void should_saveAnOrder() {
        //Given
        final Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setPrice(BigDecimal.valueOf(2030.55));
        order.setProductIds("1,2,3");
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());
        order.setLastUpdated(LocalDateTime.now());
        order.setCity("London");

        //When
        assertThat(order.getId()).isNull();
        this.orderRepository.save(order);

        //Then
        assertThat(order.getId()).isNotNull();
    }

    @Test
    @DisplayName("Repository should retrieve an order by it's order id")
    public void should_retrieveAnOrder_byOrderId() {
        //Given
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order();
        order.setOrderId(orderId);
        order.setPrice(BigDecimal.valueOf(2030.55));
        order.setProductIds("1,2,3");
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());
        order.setLastUpdated(LocalDateTime.now());
        order.setCity("London");
        this.testEntityManager.persist(order);

        //When
        final Optional<Order> persistedOrder = this.orderRepository.findByOrderId(orderId);

        //Then
        assertThat(persistedOrder.get()).isEqualTo(order);
        assertEquals(persistedOrder.get().getProductIds(), "1,2,3");
    }

    @Test
    @DisplayName("Cannot save order with null price")
    void saveOrderWithNullPrice() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final String productIds = "P01,P02,P03";
        final OrderStatus status = OrderStatus.CREATED;

        assertThatThrownBy(() -> {
            final Order order = new Order(orderId, null, productIds, status, "London",
                    LocalDateTime.now(), LocalDateTime.now());
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Cannot save order with null productIds")
    void saveOrderWithNullProductIds() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final BigDecimal price = new BigDecimal("100.00");
        final OrderStatus status = OrderStatus.CREATED;

        // When & Then
        assertThatThrownBy(() -> {
            final Order order = new Order(orderId, price, null, status, "London",
                    LocalDateTime.now(), LocalDateTime.now());
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Cannot save order with null status")
    void saveOrderWithNullStatus() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final BigDecimal price = new BigDecimal("100.00");
        final String productIds = "P01,P02,P03";

        // When & Then
        assertThatThrownBy(() -> {
            new Order(orderId, price, productIds, null, "London", LocalDateTime.now(),
                    LocalDateTime.now());
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("An order update should also create an order audit entry")
    void should_saveAuditOrder() {
        //Given
        final UUID orderId = UUID.randomUUID();
        final Order order = new Order();
        order.setOrderId(orderId);
        order.setPrice(BigDecimal.valueOf(2030.55));
        order.setProductIds("1,2,3");
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());
        order.setLastUpdated(LocalDateTime.now());
        order.setCity("London");
        final OrderAudit orderAudit = new OrderAudit(OrderStatus.PENDING_DELIVERY,
                LocalDateTime.now());
        order.getAuditItems().add(orderAudit);
        orderAudit.setOrder(order);
        this.testEntityManager.persist(order);

        //When
        final Optional<Order> persistedOrder = this.orderRepository.findByOrderId(orderId);

        //Then
        assertThat(persistedOrder.get()).isEqualTo(order);
        assertEquals(persistedOrder.get().getAuditItems().size(), 1);
        assertThat(persistedOrder.get().getAuditItems().get(0)).isEqualTo(orderAudit);
    }
}