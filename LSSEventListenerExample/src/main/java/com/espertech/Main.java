package com.espertech;

public class Main {
    public static void main(String[] args) {
        try {
            var kafkaProducer = new KafkaEventProducer("systemEvents", "localhost:9092");
            var kafkaListener = new KafkaEventListener("systemEvents", "localhost:9092");

            Thread consumerThread = new Thread(kafkaListener::run);
            consumerThread.start();

            Thread producerThread = new Thread(() -> kafkaProducer.run(8));
            producerThread.start();

            //kafkaListener.run();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}