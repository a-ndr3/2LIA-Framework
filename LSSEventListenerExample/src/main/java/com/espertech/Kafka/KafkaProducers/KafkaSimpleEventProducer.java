package com.espertech.Kafka.KafkaProducers;

import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.Main;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaSimpleEventProducer extends AbstractKafkaProducer implements LSSKafkaProducer {

    public KafkaSimpleEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        super(kafkaTopic, kafkaBootstrapServers);
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
            Main.logger.debug("Sent event: {}", record.value());
        }
    }
}
