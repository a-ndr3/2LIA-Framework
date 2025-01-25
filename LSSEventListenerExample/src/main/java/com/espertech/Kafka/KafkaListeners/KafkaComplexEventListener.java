package com.espertech.Kafka.KafkaListeners;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.ESPERQueries.SimpleEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.events.ComplexEvent;
import com.espertech.events.MySystemEvent;

import java.time.Duration;
import java.util.Collections;

public class KafkaComplexEventListener extends AbstractKafkaListener implements LSSKafkaListener {

    public KafkaComplexEventListener(String kafkaTopic,
                                    String kafkaBootstrapServers,
                                    EsperService esperService,
                                    LSSEsperQueries esperQueries) {
        super(kafkaTopic, kafkaBootstrapServers, esperService, esperQueries, ComplexEsperQueries.staticQueriesDeploymentId);
    }

    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                var records = this.consumer.poll(Duration.ofMillis(100));

                for (var record : records) {
                    //runtime.getEventService().sendEventBean(ComplexEvent.fromByteBuffer(record), "MySystemEvent");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
        }
    }
}