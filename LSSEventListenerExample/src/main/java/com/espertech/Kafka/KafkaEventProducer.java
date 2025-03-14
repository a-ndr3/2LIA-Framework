package com.espertech.Kafka;

import com.espertech.Brokers.Producers.AbstractMessageProducer;
import com.espertech.EventTypes.LSSEvent;
import com.espertech.Main;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class KafkaEventProducer extends AbstractMessageProducer {
    private final String server;
    private KafkaProducer<String, LSSEvent> producer;

    public KafkaEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        super(kafkaTopic);
        this.server = kafkaBootstrapServers;
        init();
    }

    private void init() {
        try {
            var props = new Properties();

            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 50);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
            props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());

            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

            props.put("spring.json.trusted.packages", "*");

            this.producer = new KafkaProducer<>(props);
            producer.initTransactions();

            Main.logger.info("Kafka Event BULK Producer initialized");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends LSSEvent> void sendEventBatch(List<T> events) {
        sendEvents(events, topic);
    }

    private <T extends LSSEvent> void sendEvents(List<T> events, String topic) {
        try {
            producer.beginTransaction();

            for (T event : events) {
                producer.send(new ProducerRecord<>(topic, event), (metadata, exception) -> {
                    if (exception != null) {
                        Main.logger.error("Error sending event: {}", exception.getMessage());
                    }
                });
            }
            producer.commitTransaction();
            Main.logger.info("Sent {} events to Kafka", events.size());
        } catch (Exception e) {
            Main.logger.error("Exception in Kafka producer: {}", e.getMessage());
            producer.abortTransaction();
        }
    }

    @Override
    public <T extends LSSEvent> void run(List<T> events) {
        try {
            for (int i = 0; i < events.size(); i += 1000) {
                sendEvents(events.subList(i, Math.min(i + 1000, events.size())), topic);
            }
        } catch (Exception e) {
            Main.logger.error("Exception in Kafka producer: {}", e.getMessage());
        }
    }
}

