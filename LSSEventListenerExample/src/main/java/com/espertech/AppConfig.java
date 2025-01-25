package com.espertech;

import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public KafkaEventConfig.ConfigType configType() {
        return KafkaEventConfig.ConfigType.Simple;
    }
}
