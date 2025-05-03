package com.espertech.Brokers.Producers;

import com.espertech.Brokers.BrokerType;
import com.espertech.Kafka.KafkaEventProducer;

public class MessageProducerFactory {
    public static MessageBrokerProducer createProducer(BrokerType brokerType, String topic, String brokerAddress) {
        return switch (brokerType) {
            case BrokerType.KAFKA -> new KafkaEventProducer(topic, brokerAddress);
            default -> throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        };
    }
}