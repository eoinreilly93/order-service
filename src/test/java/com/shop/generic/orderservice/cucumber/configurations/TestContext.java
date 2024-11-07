package com.shop.generic.orderservice.cucumber.configurations;

import com.shop.generic.common.kmos.OrderKMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@TestConfiguration
@Slf4j
public class TestContext {

    @Bean
    public Consumer<String, OrderKMO> consumer(final EmbeddedKafkaBroker embeddedKafkaBroker) {
        final var consumerProps = KafkaTestUtils.consumerProps("testGroup", "true",
                embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("spring.json.trusted.packages", "com.shop.generic.common.kmos");

        final ConsumerFactory<String, OrderKMO> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps);
        final Consumer<String, OrderKMO> consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "orders");
        log.info("Configured test consumer bean for embedded Kafka Broker");

        return consumer;
    }
}
