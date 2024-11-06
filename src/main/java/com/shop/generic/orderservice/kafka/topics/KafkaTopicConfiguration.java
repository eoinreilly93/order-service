package com.shop.generic.orderservice.kafka.topics;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "create-topics", havingValue = "true")
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(Topics.ORDERS.name())
                .partitions(Topics.ORDERS.getPartitions())
                .replicas(Topics.ORDERS.getReplicas())
                .build();
    }
}
