package com.espertech.Kafka.KafkaProducers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Properties;
import java.util.UUID;

public abstract class AbstractBulkKafkaProducer {
    protected String topic;
    protected String server;
    protected KafkaProducer<String, Object> producer;

    public AbstractBulkKafkaProducer(String kafkaTopic, String kafkaBootstrapServers) {
        this.topic = kafkaTopic;
        this.server = kafkaBootstrapServers;
        init();
    }
    private void init(){
        try{
        var props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        // props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());
        //props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
        System.out.println("Kafka Event BULK Producer initialized");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
