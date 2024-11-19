package com.shop.generic.orderservice.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.shop.generic.common.CommonKafkaProducerAutoConfiguration;
import com.shop.generic.common.entities.Order;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.kafka.OrderProducer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Uses an embedded kafka broker to test that orders are placed onto the topic
 * <p>
 * By default it will map a system property for embedded broker addresses
 * (spring.embedded.kafka.brokers) into the Spring Boot configuration property for Apache Kafka
 * (spring.kafka.bootstrap-servers), so you don't need configure any property values. See <a
 * href="https://docs.spring.io/spring-boot/reference/messaging/kafka.html#messaging.kafka.embedded">the
 * spring-kafka docs</a>
 * <p>
 * We use @DirtiesContext so that the topic is cleared after each test.
 */
@SpringBootTest(classes = {ShippingService.class, OrderProducer.class,
        CommonKafkaProducerAutoConfiguration.class})
@EmbeddedKafka(partitions = 1, topics = {"orders"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShippingServiceTest {

    @Autowired
    private ShippingService shippingService;

    private Consumer<String, OrderKMO> consumer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void setUp() {
        // Set up Kafka consumer properties
        final var consumerProps = KafkaTestUtils.consumerProps("testGroup", "true",
                this.embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("spring.json.trusted.packages",
                "com.shop.generic.common.kmos");

        // Create a Kafka consumer
        final ConsumerFactory<String, OrderKMO> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps);
        consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "orders");
    }

    @DisplayName("Sends a single order to the 'orders' topic")
    @Test
    void testCreateOrderShippingRequest_singleOrder() {
        // Create an Order
        final Order order = new Order(UUID.randomUUID(), new BigDecimal("100.00"), "1,2,3",
                OrderStatus.CREATED, "London", LocalDateTime.now(), LocalDateTime.now());

        // When and order is sent to the topic
        shippingService.createOrderShippingRequest(order);

        // Then check if the message was place onto the topic
        final ConsumerRecord<String, OrderKMO> received = KafkaTestUtils.getSingleRecord(consumer,
                "orders");
        assertThat(received.value()).isEqualTo(new OrderKMO(order));
    }

    @DisplayName("Sends multiple orders to the 'order' topic")
    @Test
    void testCreateOrderShippingReques_multipleMessages() {
        // Create a list to hold the 50 orders
        final List<Order> orders = new ArrayList<>();

        // Generate and send 50 orders
        for (int i = 0; i < 50; i++) {
            final Order order = new Order(UUID.randomUUID(), new BigDecimal("100.00"), "123,456",
                    OrderStatus.CREATED, "London", LocalDateTime.now(), LocalDateTime.now());
            orders.add(order);

            // Given
            shippingService.createOrderShippingRequest(order);
        }

        final List<OrderKMO> consumedMessages = new ArrayList<>();
        final Iterable<ConsumerRecord<String, OrderKMO>> records = KafkaTestUtils.getRecords(
                consumer);

        for (final ConsumerRecord<String, OrderKMO> record : records) {
            consumedMessages.add(record.value());
        }

        // Assert that we received exactly 50 messages
        assertThat(consumedMessages).hasSize(50);

        // Assert that all orders match what was sent
        for (int i = 0; i < 50; i++) {
            final OrderKMO expectedOrderKMO = new OrderKMO(orders.get(i));
            assertThat(consumedMessages.get(i)).isEqualTo(expectedOrderKMO);
        }
    }
}