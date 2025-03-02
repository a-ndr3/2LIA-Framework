package com.espertech.Kafka;

public interface LSSKafkaListener {
    void run() throws InterruptedException;
    void stop();
}
