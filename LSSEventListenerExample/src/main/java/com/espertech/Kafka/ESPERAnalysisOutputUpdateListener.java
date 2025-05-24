package com.espertech.Kafka;

import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.AnalysisCore.TopicWatcherService;
import com.espertech.AnalysisCore.Types.SpanEvent;
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

    public ESPERAnalysisOutputUpdateListener(MessageBrokerProducer producer) {
        this.producer = producer;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement stmt, EPRuntime runtime) {
        if (newEvents != null) {
            for (EventBean e : newEvents) {
                var event = (LSSEvent) e.getUnderlying();
                String topic = IssueTopicHelper.getTopicForEvent(event, stmt.getName());
                eventBuffers.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>())).add(event);

                PrometheusMetrics.esperEventCounter.inc();

                if (!eventBuffers.get(topic).isEmpty()) {
                    flushBuffer(topic);
                }
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
}

