package com.espertech.Kafka;

import com.espertech.Brokers.Listeners.MessageBrokerListener;
import com.espertech.Brokers.Listeners.MessageListenerFactory;
import com.espertech.Brokers.Producers.MessageBrokerProducer;
import com.espertech.Brokers.Producers.MessageProducerFactory;
import com.espertech.ESPERQueries.ComplexeventQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.KafkaProducers.ComplexEventGenerator;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("kafkaEventServiceV1")
@Scope("singleton")
public class KafkaEventService {
    private final KafkaEventConfig config;
    private final EsperService esperService;

    private MessageBrokerProducer producer;
    private MessageBrokerListener listener;

    private Thread producerThread;
    private Thread consumerThread;

    private volatile boolean running = false;
    @Autowired
    public KafkaEventService(KafkaEventConfig config, EsperService esperService) {
        this.config = config;
        this.esperService = esperService;
    }

    public synchronized void startAnalysis(String brokerType) {
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

        producerThread = new Thread(() -> {
            try {
                producer.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                producer.stop();
            }
        });

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
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };

        return MessageListenerFactory.createListener("kafka", config.getKafkaTopic(), config.getKafkaBootstrapServers(), esperService, queries, ComplexEsperQueries.staticQueriesDeploymentId);
    }
}
