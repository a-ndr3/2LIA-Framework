package com.espertech.Brokers.Producers;

import com.espertech.EventTypes.LSSEvent;

import java.util.List;

public interface MessageBrokerProducer {
    <T extends LSSEvent> void sendEventBatch(List<T> events);
    void run() throws InterruptedException;
    void stop();
}

