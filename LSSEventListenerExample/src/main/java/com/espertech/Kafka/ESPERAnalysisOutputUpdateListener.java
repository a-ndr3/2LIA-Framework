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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

public class ESPERAnalysisOutputUpdateListener implements UpdateListener {
    private final MessageBrokerProducer producer;
    private final Map<String, List<LSSEvent>> eventBuffers = new ConcurrentHashMap<>();
    private Timer timer = new Timer("ESPERAnalysisOutputUpdateListenerTimer", true);
    private TimerTask task;
    public ESPERAnalysisOutputUpdateListener(MessageBrokerProducer producer) {
        this.producer = producer;
        this.task = new TimerTask() { //TODO fix (sent 1 event to kakfa) by introducing timer
            @Override
            public void run() { //TODO fix it because it crashes at some point
                checkBuffer();
            }
        };
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement stmt, EPRuntime runtime) {
        if (newEvents != null) {
            for (EventBean e : newEvents) {
                var event = (LSSEvent) e.getUnderlying();
                String topic = IssueTopicHelper.getTopicForEvent(event, stmt.getName());
                eventBuffers.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>())).add(event);

                PrometheusMetrics.esperEventCounter.inc(1.0);

                try {
                    timer.schedule(task, 10000);  //TODO fix it because it crashes at some point
                }
                catch (Exception ex){
                    continue;
                }
                //if (!eventBuffers.get(topic).isEmpty()) {
                    //flushBuffer(topic);
                //}
            }
        }
    }
    private void checkBuffer() {
        for (String topic : eventBuffers.keySet()) {
            if (!eventBuffers.get(topic).isEmpty()) {
                flushBuffer(topic);
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

