package NotUsed;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;


import java.util.Properties;

public abstract class AbstractKafkaProducer {
    protected String topic;
    protected String server;
    protected KafkaProducer<String, Object> producer;

    protected volatile boolean running = true;

    public AbstractKafkaProducer(String kafkaTopic, String kafkaBootstrapServers) {
        this.topic = kafkaTopic;
        this.server = kafkaBootstrapServers;
        init();
    }
    private void init(){
        var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        this.producer = new KafkaProducer<>(properties);
        System.out.println("Kafka Event Producer initialized");
    }
}