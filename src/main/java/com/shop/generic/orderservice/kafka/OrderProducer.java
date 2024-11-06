package com.shop.generic.orderservice.kafka;

import com.shop.generic.common.kmos.OrderKMO;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer implements Producer<OrderKMO> {

    private final KafkaTemplate<String, OrderKMO> kafkaTemplate;

    @Override
    public CompletableFuture<SendResult<String, OrderKMO>> send(final OrderKMO order) {
        final CompletableFuture<SendResult<String, OrderKMO>> result = this.kafkaTemplate.send(
                "orders", order);
        log.info("Sent order: {} to kafka topic 'orders'", order);
        this.kafkaTemplate.flush();
        return result;
    }
}
