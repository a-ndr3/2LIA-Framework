package com.espertech;

import com.espertech.Kafka.KafkaEventService;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaEventController {

    @Qualifier("kafkaEventServiceV1")
    private final KafkaEventService kafkaEventService;
    private final KafkaEventConfig kafkaEventConfig;

    @Autowired
    public KafkaEventController(KafkaEventService kafkaEventService, KafkaEventConfig kafkaEventConfig) {
        this.kafkaEventService = kafkaEventService;
        this.kafkaEventConfig = kafkaEventConfig;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startAnalysis(@RequestParam(defaultValue = "complex", name = "producer") String producer,
                                                @RequestParam(defaultValue = "complex", name = "listener") String listener) {
        kafkaEventService.startAnalysis(listener);
        return ResponseEntity.ok("Kafka Analysis started with producer: " + producer + " and listener: " + listener);
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopAnalysis() {
        kafkaEventService.stopAnalysis();
        return ResponseEntity.ok("Kafka Analysis stopped.");
    }

    @PostMapping("/config")
    public ResponseEntity<String> updateConfig(@RequestParam String type) {
        KafkaEventConfig.ConfigType configType = KafkaEventConfig.ConfigType.valueOf(type);
        kafkaEventConfig.updateConfig(configType);
        return ResponseEntity.ok("Kafka configuration updated to: " + type);
    }
}

