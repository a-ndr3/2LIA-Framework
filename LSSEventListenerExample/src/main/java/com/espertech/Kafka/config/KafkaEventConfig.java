package com.espertech.Kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class KafkaEventConfig {

    public enum ConfigType {
        Simple,
        Complex
    }

    private final String kafkaTopic;
    private final String kafkaBootstrapServers;

    public KafkaEventConfig(ConfigType configType) {
        switch (configType) {
            case Simple:
                this.kafkaTopic = "systemEvents";
                this.kafkaBootstrapServers = "localhost:9092";
                break;
            case Complex:
                this.kafkaTopic = "complexEvents";
                this.kafkaBootstrapServers = "localhost:9092";
                break;
            default:
                throw new IllegalArgumentException("Unknown ConfigType: " + configType);
        }
    }

    @Bean
    public String kafkaTopic() {
        return kafkaTopic;
    }

    @Bean
    public String kafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }
}
