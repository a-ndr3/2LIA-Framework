package com.espertech.Kafka;

import org.springframework.stereotype.Service;

@Service
public class KafkaEventService {
    private final LSSKafkaProducer producer;
    private final LSSKafkaListener listener;

    public KafkaEventService(
            LSSKafkaProducer producer,
            LSSKafkaListener listener) {
        this.producer = producer;
        this.listener = listener;
        startThreads();
    }

    private void startThreads() {
        Thread consumerThread = new Thread(listener::run);
        consumerThread.start();

        Thread producerThread = new Thread(producer::run);
        producerThread.start();
    }
}
