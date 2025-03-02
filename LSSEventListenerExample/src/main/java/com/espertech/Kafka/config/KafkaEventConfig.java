package com.espertech.Kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaEventConfig {

    @Value("${kafka.topic:complexEvents}")
    public String kafkaTopic;

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    public String kafkaBootstrapServers;

    private ConfigType configType = ConfigType.Complex;

    public KafkaEventConfig() {
        updateConfig(configType);
    }

    public enum ConfigType {
        Simple,
        Complex
    }

    public void updateConfig(ConfigType configType) {
        this.configType = configType;
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

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }
}
