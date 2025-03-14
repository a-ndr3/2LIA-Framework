package com.espertech.Brokers.Producers;

public abstract class AbstractMessageProducer implements MessageBrokerProducer {
    protected String topic;
    protected volatile boolean running = true;

    public AbstractMessageProducer(String topic) {
        this.topic = topic;
    }

    @Override
    public void stop() {
        running = false;
    }
}


