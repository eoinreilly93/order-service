package com.shop.generic.orderservice.services;


import com.shop.generic.common.entities.Order;
import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.kafka.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class ShippingService {

    private final Producer<OrderKMO> producer;

    ShippingService(final Producer<OrderKMO> producer) {
        this.producer = producer;
    }

    void createOrderShippingRequest(final Order order) {
        final OrderKMO orderKMO = new OrderKMO(order);
        final var result = this.producer.send(orderKMO);
        log.info("Created order shipping request for order {}", order);
    }
}
