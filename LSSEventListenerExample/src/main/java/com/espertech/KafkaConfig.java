package com.espertech;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public String kafkaTopic() {
        return "systemEvents";
    }

    @Bean
    public String kafkaBootstrapServers() {
        return "localhost:9092";
    }
}
