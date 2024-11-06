package com.shop.generic.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ignoring the topic creation could also be done using @MockBean private NewTopic topic;
 * <p>
 * TODO: Eventually move topic create to be external to the app
 */
@SpringBootTest(properties = {"spring.kafka.create-topics=false"})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
