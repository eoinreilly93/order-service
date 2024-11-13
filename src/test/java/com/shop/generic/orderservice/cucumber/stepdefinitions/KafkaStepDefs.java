package com.shop.generic.orderservice.cucumber.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import io.cucumber.java.en.And;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.awaitility.Awaitility;

public class KafkaStepDefs extends CucumberSpringConfiguration {

    private static final int TIMEOUT = 5;

    @Autowired
    private Consumer<String, OrderKMO> consumer;

    @And("the order is sent to the kafka topic {string}")
    public void theOrderIsSentToTheKafkaTopic(final String topic) {
        consumer.subscribe(List.of(topic));
        Awaitility.await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            final ConsumerRecords<String, OrderKMO> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                return false;
            }
            for (final ConsumerRecord<String, OrderKMO> record : records) {
                final OrderKMO order = record.value();
                assertNotNull(order);
            }
            return true;
        });
    }

    @And("the order is not sent to the kafka topic {string}")
    public void theOrderIsNotSentToTheKafkaTopic(final String topic) {
        consumer.subscribe(List.of(topic));
        Awaitility.await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            final ConsumerRecords<String, OrderKMO> records = consumer.poll(Duration.ofMillis(100));
            return records.isEmpty();
        });
    }

    @And("kafka is up and running")
    public void kafkaIsUpAndRunning() {
        assertThat(kafkaContainer.isRunning()).isTrue();
    }
}
