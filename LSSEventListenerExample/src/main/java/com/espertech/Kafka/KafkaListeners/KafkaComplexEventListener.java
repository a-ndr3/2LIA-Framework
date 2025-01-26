package com.espertech.Kafka.KafkaListeners;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.EventListeners.ComplexEventListener;
import com.espertech.Kafka.LSSKafkaListener;

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

            var listener = new ComplexEventListener();
            runtime.getDeploymentService().getStatement(ComplexEsperQueries.staticQueriesDeploymentId, "my-statement").addListener(listener);

            while (true) {
                var records = this.consumer.poll(Duration.ofMillis(100));

                for (var record : records) {
                    runtime.getEventService().sendEventBean(record.value(), "ComplexEvent");
                    //runtime.getEventService().sendEventJson(record.value(), "ComplexEvent");
                    //runtime.getEventService().routeEventJson(record.value(), "ComplexEvent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error consuming record.");
        } finally {
            consumer.close();
        }
    }
}