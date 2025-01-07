package com.espertech;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Properties;
import java.util.UUID;

@Component
public class KafkaEventProducer {
    private final String topic;
    private final String server;
    private KafkaProducer<String, String> producer;

    public KafkaEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        this.topic = kafkaTopic;
        this.server = kafkaBootstrapServers;
        init();
    }

    private void init(){
        var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(properties);
        System.out.println("Kafka Event Producer initialized");
    }

    public void run(int runEachSeconds){
        int i = 0;
        while (true) {
            try {
                Thread.sleep(runEachSeconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            var record = new ProducerRecord<String,String>(topic, String.format("systemId:A%s,type:update%s", i, i));
            producer.send(record);

            i++;
            System.out.println("Sent event: " + record.value());
        }
    }
}
