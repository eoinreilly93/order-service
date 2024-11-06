package com.shop.generic.orderservice.kafka.topics;

import lombok.Getter;

//TODO: Should topics be defined in properties? Ideally topics should be created externally to the app anyways
@Getter
public enum Topics {

    ORDERS("orders", 1, 1);

    private final String topicName;
    private final int partitions;
    private final int replicas;

    Topics(final String topicName, final int partitions, final int replicas) {
        this.topicName = topicName;
        this.partitions = partitions;
        this.replicas = replicas;
    }
}
