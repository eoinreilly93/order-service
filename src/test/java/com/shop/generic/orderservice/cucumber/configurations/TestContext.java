package com.shop.generic.orderservice.cucumber.configurations;

import com.shop.generic.common.kmos.OrderKMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;

@TestConfiguration
@Slf4j
public class TestContext {

    @Bean
    public Consumer<String, OrderKMO> consumer(
            final ConsumerFactory<String, OrderKMO> consumerFactory) {
        final Consumer<String, OrderKMO> consumer = consumerFactory.createConsumer();
        log.info("Configured test consumer bean");
        return consumer;
    }
}
