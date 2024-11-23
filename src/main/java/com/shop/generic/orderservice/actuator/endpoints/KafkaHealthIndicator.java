package com.shop.generic.orderservice.actuator.endpoints;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KafkaHealthIndicator implements HealthIndicator {

    private final AdminClient adminClient;
    private final Set<String> requiredTopics = Set.of("orders");

    public KafkaHealthIndicator(final AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    public Health health() {
        try {
            // Check connectivity
            adminClient.listTopics(new ListTopicsOptions().timeoutMs(5000)).names()
                    .get(5, TimeUnit.SECONDS);

            // Verify topics
            final Set<String> topics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            if (topics.containsAll(requiredTopics)) {
                return Health.up().withDetail("topics", "All required topics exist").build();
            } else {
                return Health.down()
                        .withDetail("missingTopics",
                                requiredTopics.stream().filter(topic -> !topics.contains(topic)))
                        .build();
            }
        } catch (final Exception e) {
            return Health.down(e).build();
        }
    }
}
