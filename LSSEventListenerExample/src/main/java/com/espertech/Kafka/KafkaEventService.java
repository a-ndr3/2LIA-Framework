package com.espertech.Kafka;

import com.espertech.Brokers.Listeners.MessageBrokerListener;
import com.espertech.Brokers.Listeners.MessageListenerFactory;
import com.espertech.Brokers.Producers.MessageBrokerProducer;
import com.espertech.Brokers.Producers.MessageProducerFactory;
import com.espertech.ESPERQueries.DynatraceEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.EventGenerators.ComplexEventGenerator;
import com.espertech.EventGenerators.DynatraceEventGenerator;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    public synchronized void startAnalysis(String brokerType) throws IOException {
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
            case "complex":
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
            case "dynatrace":
                var eventGen = new DynatraceEventGenerator();
                var dynatraceEvents = eventGen.readFile("src/main/resources/dynatraceLogs/1.csv");
                dynatraceEvents.addAll(eventGen.readFile("src/main/resources/dynatraceLogs/2.csv"));
                producerThread = new Thread(() -> {
                    try {
                        producer.run(dynatraceEvents);
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
        return MessageProducerFactory.createProducer("kafka", config.getKafkaTopic(), config.getKafkaBootstrapServers());
    }

    private MessageBrokerListener createListener(String type) {
        LSSEsperQueries queries = switch (type) {
            case "complex" -> new ComplexEsperQueries();
            case "dynatrace" -> new DynatraceEsperQueries();
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };

        switch (type){
            case "complex":
                return MessageListenerFactory.createListener("kafka", "complexEvents", config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            case "dynatrace":
                return MessageListenerFactory.createListener("kafka", "dynatraceEvents", config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            default:
                throw new IllegalArgumentException("Unknown listener type: " + type);
        }
    }
}
