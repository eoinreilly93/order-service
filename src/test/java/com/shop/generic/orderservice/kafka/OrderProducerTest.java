package com.shop.generic.orderservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.kmos.OrderKMO;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderKMO> kafkaTemplate;

    @InjectMocks
    private OrderProducer orderProducer;


    @Test
    void testSendOrderToKafkaTopic() {
        final OrderKMO orderKMO = new OrderKMO(
                UUID.randomUUID(),
                BigDecimal.valueOf(100.0),
                "123,124",
                OrderStatus.CREATED,
                "London"
        );
        // Prepare a future result
        final CompletableFuture<SendResult<String, OrderKMO>> future = new CompletableFuture<>();
        future.complete(null);  // Complete the future with a null value

        // Mock Kafka template behavior
        when(kafkaTemplate.send(eq("orders"), eq(orderKMO)))
                .thenReturn(future);

        // When
        final CompletableFuture<SendResult<String, OrderKMO>> result = orderProducer.send(orderKMO);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("orders"), eq(orderKMO));
        assertThat(result).isCompletedWithValue(null);
    }
}