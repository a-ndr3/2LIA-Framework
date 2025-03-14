package com.espertech.Brokers.Listeners;

public interface MessageBrokerListener {
    void startListening() throws InterruptedException;;
    void stopListening();
}

