package com.shop.generic.orderservice.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;

public interface Producer<T> {

    /**
     * Send an object of type T to a kakfa topic
     *
     * @param object defined by the implementing class
     * @return the result of the send
     */
    CompletableFuture<SendResult<String, T>> send(T object);

}
