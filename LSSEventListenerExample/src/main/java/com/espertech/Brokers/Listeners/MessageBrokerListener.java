package com.espertech.Brokers.Listeners;

import com.espertech.esper.runtime.client.UpdateListener;

public interface MessageBrokerListener {
    void startListening() throws InterruptedException;;
    void stopListening();
    UpdateListener getListener();
}

