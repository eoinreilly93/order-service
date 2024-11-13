package com.shop.generic.orderservice.cucumber.stepdefinitions;

import com.shop.generic.common.kmos.OrderKMO;
import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import com.shop.generic.orderservice.repositories.OrderRepository;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class HooksStepDefs extends CucumberSpringConfiguration {

    @Autowired
    private OrderRepository orderRepository;

    // Static consumer required so that it can be closed in the @AfterAll method (which must be static)
    // If this isn't done, the kafka testcontainer stops but the consumer is still running and the tests will continue to run for the default consumer timeout
    private static Consumer<String, OrderKMO> staticConsumer;

    //Spring handles dependency injection via the non-static @Autowired method, allowing you to bridge the gap between Spring’s context and static logic.
    @Autowired
    private void setConsumer(final Consumer<String, OrderKMO> consumer) {
        staticConsumer = consumer;
    }

    @Before
    public void before() {
        log.info("In HooksStepDefs.before()");
    }

    @After
    public void after() {
        log.info("In HooksStepDef.after()");
        this.orderRepository.deleteAll(); //TODO: Fix this by using @DirtiesContext instead
    }

    @AfterAll
    public static void afterAll() {
        log.info("Closing kafka test consumer");
        staticConsumer.close();
    }
}
