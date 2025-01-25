package com.espertech.Kafka.config;

import com.espertech.events.ComplexEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaEventConfig {

//    @Bean
//
//    public ProducerFactory<String, ComplexEvent> producerFactory() {
//        Map<String, Object> config = new HashMap<>();
//
//        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//                "localhost:9092");
//        config.put(
//                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        config.put(
//                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        config.put(
//                "Config_Type_Simple",
//                "systemEvents");
//        config.put(
//                "Config_Type_Complex",
//                "complexEvents");
//
//        return new DefaultKafkaProducerFactory<>(config);
//    }

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

//    @Bean
//    public KafkaTemplate kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
}
