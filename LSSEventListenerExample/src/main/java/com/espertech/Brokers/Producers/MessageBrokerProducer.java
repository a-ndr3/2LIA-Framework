package com.espertech.Brokers.Producers;

import com.espertech.EventTypes.LSSEvent;

import java.util.List;

public interface MessageBrokerProducer {
    <T extends LSSEvent> void sendEventBatch(List<T> events);
    <T extends LSSEvent> void run(List<T> events) throws InterruptedException;
    void stop();
}

