package com.espertech.Kafka;

import com.espertech.Kafka.KafkaProducers.AbstractBulkKafkaProducer;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.espertech.events.LSSKafkaEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ESPERAnalysisOutputUpdateListener implements UpdateListener {
    private final AbstractBulkKafkaProducer kafkaProducer;
    private final Map<String, List<LSSKafkaEvent>> eventBuffers = new ConcurrentHashMap<>();
    private final int BATCH_SIZE = 100; //TODO: what to do if we never reach this size

    public ESPERAnalysisOutputUpdateListener(AbstractBulkKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement stmt, EPRuntime runtime) {
        if (newEvents != null) {
            for (EventBean e : newEvents) {
                var event = (LSSKafkaEvent) e.getUnderlying();
                String topic = getTopicForEvent(event);
                eventBuffers.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>())).add(event);

                if (eventBuffers.get(topic).size() >= BATCH_SIZE) {
                    flushBuffer(topic);
                }
            }
        }
    }

    private void flushBuffer(String topic) {
        List<LSSKafkaEvent> batch;
        synchronized (eventBuffers.get(topic)) {
            batch = new ArrayList<>(eventBuffers.get(topic));
            eventBuffers.get(topic).clear();
        }
        kafkaProducer.sendEventBatch(batch, topic);
    }

    private String getTopicForEvent(LSSKafkaEvent event) {
        return "esper-" + event.getClass().getSimpleName().toLowerCase();
    }
}

