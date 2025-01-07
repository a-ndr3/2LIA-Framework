package com.espertech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {
    static EsperService esperService; //todo inject into controller
    public static void main(String[] args) {
        try {
            esperService = new EsperServiceImpl();
            SpringApplication.run(Main.class, args);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
    //http://localhost:8081/swagger-ui/index.html
    @Bean
    public KafkaEventProducer kafkaEventProducer() {
        return new KafkaEventProducer("systemEvents", "localhost:9092");
    }

    @Bean
    public KafkaEventListener kafkaEventListener() {
        return new KafkaEventListener("systemEvents", "localhost:9092", esperService);
    }
}