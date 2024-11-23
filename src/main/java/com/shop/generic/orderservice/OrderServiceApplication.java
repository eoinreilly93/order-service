package com.shop.generic.orderservice;

import com.shop.generic.common.CommonKafkaConsumerAutoConfiguration;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@EntityScan("com.shop.generic.common.entities")
@SpringBootApplication(exclude = {CommonKafkaConsumerAutoConfiguration.class})
public class OrderServiceApplication {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static void main(final String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /**
     * Used as part of the
     * {@link com.shop.generic.orderservice.actuator.endpoints.KafkaHealthIndicator} for spring boot
     * admin
     */
    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        ));
    }
}
