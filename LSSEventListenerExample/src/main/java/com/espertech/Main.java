package com.espertech;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.Kafka.KafkaListeners.KafkaComplexEventListener;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventBulkProducer;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    static EsperService esperService; //todo inject into controller
    static KafkaEventConfig config = new KafkaEventConfig(KafkaEventConfig.ConfigType.Complex);

    public static void main(String[] args) {
        try {
            logger.info("Application started");
            esperService = new EsperServiceImpl();
            SpringApplication.run(Main.class, args);
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
        }
    }

    //http://localhost:8081/swagger-ui/index.html

    @Bean
    public LSSKafkaProducer kafkaEventProducer() {
        return new KafkaComplexEventBulkProducer(config.kafkaTopic(), config.kafkaBootstrapServers());
    }

    @Bean
    public LSSKafkaListener kafkaEventListener() {
        LSSEsperQueries queries = new ComplexEsperQueries();
        return new KafkaComplexEventListener(config.kafkaTopic(), config.kafkaBootstrapServers(), esperService, queries);
    }
}