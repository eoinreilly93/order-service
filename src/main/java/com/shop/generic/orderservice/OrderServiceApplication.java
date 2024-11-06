package com.shop.generic.orderservice;

import com.shop.generic.common.CommonKafkaConsumerAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.shop.generic.common.entities")
@SpringBootApplication(exclude = {CommonKafkaConsumerAutoConfiguration.class})
public class OrderServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
