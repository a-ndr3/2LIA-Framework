package com.espertech.Kafka;

import com.espertech.Brokers.Producers.MessageBrokerProducer;
import com.espertech.PrometheusMetrics.PrometheusMetrics;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.espertech.EventTypes.LSSEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

public class ESPERAnalysisOutputUpdateListener implements UpdateListener {
    private final MessageBrokerProducer producer;
    private final Map<String, List<LSSEvent>> eventBuffers = new ConcurrentHashMap<>();
    private final int BATCH_SIZE = 10;

    public ESPERAnalysisOutputUpdateListener(MessageBrokerProducer producer) {
        this.producer = producer;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement stmt, EPRuntime runtime) {
        if (newEvents != null) {
            for (EventBean e : newEvents) {
                var event = (LSSEvent) e.getUnderlying();
                String topic = getTopicForEvent(event, stmt.getName());
                eventBuffers.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>())).add(event);

                PrometheusMetrics.esperAlertCounter.inc();

                if (eventBuffers.get(topic).size() >= BATCH_SIZE) {
                    flushBuffer(topic);
                }

                PrometheusMetrics.eventBufferSize.set(eventBuffers.get(topic).size());
            }
        }
    }

    private void flushBuffer(String topic) {
        List<LSSEvent> batch;
        synchronized (eventBuffers.get(topic)) {
            batch = new ArrayList<>(eventBuffers.get(topic));
            eventBuffers.get(topic).clear();
        }
        producer.sendEventBatch(batch, topic);
    }

    private String getTopicForEvent(LSSEvent event ,String statementName) {
        return "esper-" + event.getClass().getSimpleName().toLowerCase() + "-" + statementName;
    }
}

