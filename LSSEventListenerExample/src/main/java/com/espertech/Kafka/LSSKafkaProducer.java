package com.espertech.Kafka;

public interface LSSKafkaProducer {
    void run() throws InterruptedException;
    void stop();
}
