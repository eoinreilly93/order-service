package com.shop.generic.orderservice.cucumber.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import io.cucumber.java.en.And;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

public class KafkaStepDefs extends CucumberSpringConfiguration {

    @Autowired
    private Consumer<String, OrderKMO> consumer;

    @And("the order is sent to the kafka topic {string}")
    public void theOrderIsSentToTheKafkaTopic(final String topic) {
        final ConsumerRecord<String, OrderKMO> received = KafkaTestUtils.getSingleRecord(
                this.consumer,
                topic);
        assertThat(received).isNotNull();
    }
}
