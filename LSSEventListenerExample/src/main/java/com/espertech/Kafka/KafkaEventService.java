package com.espertech.Kafka;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.KafkaListeners.KafkaComplexEventListener;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventBulkProducer;
import com.espertech.Kafka.config.KafkaEventConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("kafkaEventServiceV1")
@Scope("singleton")
public class KafkaEventService {
    private final KafkaEventConfig config;
    private final EsperService esperService;

    private LSSKafkaProducer producer;
    private LSSKafkaListener listener;

    private Thread producerThread;
    private Thread consumerThread;

    private volatile boolean running = false;
    @Autowired
    public KafkaEventService(KafkaEventConfig config, EsperService esperService) {
        this.config = config;
        this.esperService = esperService;
    }

    public synchronized void startAnalysis(String producerType, String listenerType) {
        stopAnalysis();

        producer = createProducer(producerType);
        listener = createListener(listenerType);

        running = true;

        consumerThread = new Thread(() -> {
            try {
                listener.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                listener.stop();
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
            listener.stop();
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

    private LSSKafkaProducer createProducer(String type) {
        return switch (type) {
            case "complex" -> new KafkaComplexEventBulkProducer(config.getKafkaTopic(), config.getKafkaBootstrapServers());
            default -> throw new IllegalArgumentException("Unknown producer type: " + type);
        };
    }

    private LSSKafkaListener createListener(String type) {
        LSSEsperQueries queries = switch (type) {
            case "complex" -> new ComplexEsperQueries();
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };
        return new KafkaComplexEventListener(config.getKafkaTopic(), config.getKafkaBootstrapServers(), esperService, queries);
    }
}
