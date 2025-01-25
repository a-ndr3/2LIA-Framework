package com.espertech.Kafka.KafkaProducers;

import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.Main;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaComplexEventProducer extends AbstractKafkaProducer implements LSSKafkaProducer {

    public KafkaComplexEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        super(kafkaTopic, kafkaBootstrapServers);
    }

    public void run(int runEachSeconds){

    }
}
