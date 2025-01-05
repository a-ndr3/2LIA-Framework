package com.espertech;

public class Main {
    public static void main(String[] args) {
        try {
            var kafkaProducer = new KafkaEventProducer("systemEvents", "localhost:9092");
            var kafkaListener = new KafkaEventListener("systemEvents", "localhost:9092");

            Thread producerThread = new Thread(() -> kafkaProducer.run(5));
            producerThread.start();

            //Thread consumerThread = new Thread(kafkaListener::run);
            //consumerThread.start();

            kafkaListener.run();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}