package com.espertech.Kafka;

import com.espertech.Brokers.BrokerType;
import com.espertech.Brokers.Listeners.MessageBrokerListener;
import com.espertech.Brokers.Listeners.MessageListenerFactory;
import com.espertech.Brokers.Producers.MessageBrokerProducer;
import com.espertech.Brokers.Producers.MessageProducerFactory;
import com.espertech.ESPERQueries.DynatraceAnalysisEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EventGenerators.ComplexEventGenerator;
import com.espertech.EventGenerators.DynatraceEventGenerator;
import com.espertech.EventTypes.Types.dynatrace.DynatraceLog;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service("kafkaEventServiceV1")
@Scope("singleton")
public class KafkaEventService {
    private final KafkaEventConfig config;

    private MessageBrokerProducer producer;
    private MessageBrokerListener listener;

    private Thread producerThread;
    private Thread consumerThread;

    private volatile boolean running = false;

    @Autowired
    public KafkaEventService(KafkaEventConfig config) {
        this.config = config;
    }

    public synchronized void startAnalysis(KafkaEventConfig.ConfigType brokerType) throws IOException {
        stopAnalysis();

        producer = createProducer();
        listener = createListener(brokerType);

        running = true;

        consumerThread = new Thread(() -> {
            try {
                listener.startListening();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }finally {
                listener.stopListening();
            }
        });

        switch (brokerType)
        {
            case KafkaEventConfig.ConfigType.Complex:
                var complexEvents = ComplexEventGenerator.getEvents();
                producerThread = new Thread(() -> {
//                    try {
//                        producer.run(complexEvents);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    } finally {
//                        producer.stop();
//                    }
                });
                break;
            case KafkaEventConfig.ConfigType.Dynatrace:
                ClassPathResource timeframe1 = new ClassPathResource("traces_spans_astroshop_timeframe1.json");
                ClassPathResource timeframe2 = new ClassPathResource("traces_spans_astroshop_timeframe2.json");
                var eventGen = new DynatraceEventGenerator.Builder(timeframe1.getFile()).setGroupByTraceId(true)
                        .build();

                var logs = new ArrayList<DynatraceLog>();
                logs.add(eventGen.log);

                eventGen.readFile(timeframe2.getFile());
                eventGen.groupByTraceId();
                logs.add(eventGen.log);

                var allRecords = logs.stream().flatMap(log -> log.records.stream()).toList();

                producerThread = new Thread(() -> {
                    try {
                        producer.run(allRecords);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producer.stop();
                    }
                });
                break;
        }

        consumerThread.start();
        producerThread.start();
    }

    public synchronized void stopAnalysis() {
        if (!running) return;

        running = false;

        if (producer != null) {
            producer.stop();
        }
        if (listener != null) {
            listener.stopListening();
        }

        if (producerThread != null && producerThread.isAlive()) {
            producerThread.interrupt();
            try {
                producerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();
            try {
                consumerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        producerThread = null;
        consumerThread = null;
    }

    private MessageBrokerProducer createProducer() {
        return MessageProducerFactory.createProducer(BrokerType.KAFKA, config.getKafkaTopic(), config.getKafkaBootstrapServers());
    }

    private MessageBrokerListener createListener(KafkaEventConfig.ConfigType type) {
        LSSEsperQueries queries = switch (type) {
            case KafkaEventConfig.ConfigType.Complex -> new ComplexEsperQueries();
            case KafkaEventConfig.ConfigType.Dynatrace -> new DynatraceAnalysisEsperQueries();
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };

        switch (type){
            case KafkaEventConfig.ConfigType.Complex:
                return MessageListenerFactory.createListener(BrokerType.KAFKA, KafkaEventConfig.ConfigType.Complex, config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            case KafkaEventConfig.ConfigType.Dynatrace:
                return MessageListenerFactory.createListener(BrokerType.KAFKA, KafkaEventConfig.ConfigType.Dynatrace, config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            default:
                throw new IllegalArgumentException("Unknown listener type: " + type);
        }
    }
}
