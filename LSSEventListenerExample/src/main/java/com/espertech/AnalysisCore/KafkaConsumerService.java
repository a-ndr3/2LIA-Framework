package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.Kafka.KafkaListeners.KafkaConsumerFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public class KafkaConsumerService {
    public static void startListening(String topic, Consumer<SpanEvent> handler) {
        new Thread(() -> {
            KafkaConsumer<String, SpanEvent> consumer = KafkaConsumerFactory.create();
            consumer.subscribe(List.of(topic));

            while (true) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    handler.accept(record.value());
                }
            }
        }).start();
    }
}
