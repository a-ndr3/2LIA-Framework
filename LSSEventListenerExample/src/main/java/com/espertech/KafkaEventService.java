package com.espertech;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventService {
    private final KafkaEventProducer producer;
    private final KafkaEventListener listener;

    public KafkaEventService(KafkaEventProducer producer, KafkaEventListener listener) {
        this.producer = producer;
        this.listener = listener;
        startThreads();
    }

    private void startThreads() {
        Thread consumerThread = new Thread(listener::run);
        consumerThread.start();

        Thread producerThread = new Thread(() -> producer.run(8));
        producerThread.start();
    }
}
