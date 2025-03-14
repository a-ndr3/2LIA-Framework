package com.espertech.Brokers.Producers;

import com.espertech.Kafka.KafkaEventProducer;

public class MessageProducerFactory {
    public static MessageBrokerProducer createProducer(String brokerType, String topic, String brokerAddress) {
        return switch (brokerType.toLowerCase()) {
            case "kafka" -> new KafkaEventProducer(topic, brokerAddress);
            default -> throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        };
    }
}