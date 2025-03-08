package com.espertech.Kafka.KafkaListeners;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.ESPERAnalysisOutputUpdateListener;
import com.espertech.Kafka.KafkaProducers.AbstractBulkKafkaProducer;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventBulkProducer;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.Main;
import com.espertech.PrometheusMetrics.PrometheusMetrics;
import com.espertech.events.LSSKafkaEvent;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.Collections;

public class KafkaComplexEventListener extends AbstractKafkaListener implements LSSKafkaListener {
    ESPERAnalysisOutputUpdateListener listener = new ESPERAnalysisOutputUpdateListener(
            new KafkaComplexEventBulkProducer(Main.config.kafkaTopic, Main.config.kafkaBootstrapServers));

    public KafkaComplexEventListener(String kafkaTopic,
                                     String kafkaBootstrapServers,
                                     EsperService esperService,
                                     LSSEsperQueries esperQueries) {
        super(kafkaTopic, kafkaBootstrapServers, esperService, esperQueries, ComplexEsperQueries.staticQueriesDeploymentId);
    }

    public void run() {
        try {
            runtime.getDeploymentService().getStatement(ComplexEsperQueries.staticQueriesDeploymentId, "my-statement").addListener(listener);

            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = null;

            long allSizes = 0;
            long pollCounter = 0;
            long totalPollTime = 0;
            long totalInjectTime = 0;
            while (running) {
                long pollStart = System.nanoTime();
                records = consumer.poll(Duration.ofMillis(100));

                PrometheusMetrics.esperEventCounter.inc(records.count());

                long pollDuration = System.nanoTime() - pollStart;
                long size = records.count();
                long injectStart = System.nanoTime();
                for (var record : records) {
                    runtime.getEventService().sendEventBean(record.value(), "ComplexEvent");
                }
                long injectDuration = System.nanoTime() - injectStart;

                if (size != 0) {
                    totalPollTime += pollDuration;
                    totalInjectTime += injectDuration;
                    pollCounter++;
                }

                if (size != 0) {
                    System.out.printf("Size: %d, Poll Time: %d ms, Inject Time: %d ms%n",
                            size, pollDuration / 1_000_000, injectDuration / 1_000_000);
                    allSizes += size;
                } else {
                    if (allSizes != 0)
                        System.out.printf("Overall Consumed: %d, Average Poll Time: %.2f ms%n, Average Inject Time: %.2f ms%n",
                            allSizes, (totalPollTime / pollCounter) / 1_000_000.0, (totalInjectTime / pollCounter) / 1_000_000.0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error consuming record.");
        } finally {
            consumer.close();
        }
    }

    @Override
    public void stop() {
        running = false;
    }
}