package com.espertech.Brokers.Producers;

import com.espertech.Kafka.KafkaEventProducer;

public class MessageProducerFactory {
    public static MessageBrokerProducer createProducer(String brokerType, String topicOrQueue, String brokerAddress) {
        return switch (brokerType.toLowerCase()) {
            case "kafka" -> new KafkaEventProducer(topicOrQueue, brokerAddress);
            default -> throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        };
    }
}