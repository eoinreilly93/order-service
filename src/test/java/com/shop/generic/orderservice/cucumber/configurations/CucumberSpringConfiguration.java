package com.shop.generic.orderservice.cucumber.configurations;

import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.AbstractBaseIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContext.class)
@ActiveProfiles("cucumber")
@AutoConfigureWireMock(port = 0) //Random port
@EmbeddedKafka(partitions = 1, topics = {"orders"}) //TODO: Replace this with kafka test containers
public class CucumberSpringConfiguration extends AbstractBaseIntegrationTest {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;

    protected Consumer<String, OrderKMO> consumer;

}
