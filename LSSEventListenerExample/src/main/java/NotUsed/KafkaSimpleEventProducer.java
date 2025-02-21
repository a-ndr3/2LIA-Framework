package NotUsed;

import com.espertech.Kafka.LSSKafkaProducer;

public class KafkaSimpleEventProducer extends AbstractKafkaProducer implements LSSKafkaProducer {

    public KafkaSimpleEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        super(kafkaTopic, kafkaBootstrapServers);
    }

    public void run(){
        int i = 0;
        while (true) {
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //var record = new ProducerRecord<String,String>(topic, String.format("systemId:A%s,type:update%s", i, i));
            //producer.send(record);

            i++;
            //Main.logger.debug("Sent event: {}", record.value());
        }
    }
}
