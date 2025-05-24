package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.SpanEventConverter;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.Kafka.KafkaListeners.KafkaConsumerFactory;
import com.espertech.esper.common.internal.collection.Pair;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public class KafkaConsumerService {
    public static void startListening(String topic, Consumer<DynatraceRecord> handler) {
        new Thread(() -> {
            KafkaConsumer<String, DynatraceRecord> consumer = KafkaConsumerFactory.create("AnalysisServiceGroup-" + topic);
            consumer.subscribe(List.of(topic));
            var error = false;

            while (true) {
                error = false;
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    try {
                        handler.accept(record.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                        error = true;
                    }
                }
                if (!error && !records.isEmpty())
                    consumer.commitSync();
            }
        }).start();
    }
}
