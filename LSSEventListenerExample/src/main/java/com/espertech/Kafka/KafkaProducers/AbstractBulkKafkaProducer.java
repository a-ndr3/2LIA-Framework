package com.espertech.Kafka.KafkaProducers;

import com.espertech.Main;
import com.espertech.events.LSSKafkaEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

public abstract class AbstractBulkKafkaProducer {
    protected String topic;
    protected String server;
    protected KafkaProducer<String, LSSKafkaEvent> producer;

    public AbstractBulkKafkaProducer(String kafkaTopic, String kafkaBootstrapServers) {
        this.topic = kafkaTopic;
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

            System.out.println("Kafka Event BULK Producer initialized");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends LSSKafkaEvent> void sendEventBatch(List<T> events) {
        sendEvents(events, topic);
    }

    public <T extends LSSKafkaEvent> void sendEventBatch(List<T> events, String topic) {
        Main.topicManager.createTopicIfNotExists(topic);
        sendEvents(events, topic);
    }

    private <T extends LSSKafkaEvent> void sendEvents(List<T> events, String topic) {
        try {
            producer.beginTransaction();

            for (T event : events) {
                producer.send(new ProducerRecord<>(topic, event), (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Error sending event: " + exception.getMessage());
                    }
                });
            }
            producer.commitTransaction();
        } catch (Exception e) {
            System.err.println("Exception in Kafka producer: " + e.getMessage());
            producer.abortTransaction();
        }
    }
}
