package com.espertech.EventListeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.espertech.events.ComplexEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ComplexEventListener implements UpdateListener {
    private List<ComplexEvent> emittedList = new LinkedList<>();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        for (EventBean newEvent : newEvents) {
            var event = ((HashMap) newEvent.getUnderlying());
            var eventId = event.get("eventId").toString();
            var eventType = event.get("eventType").toString();
            var value = event.get("value").toString();
            emittedList.add(new ComplexEvent(
                    Integer.parseInt(eventId),
                    eventType,
                    Double.parseDouble(value)));
            System.out.printf("""
                            Event Type: %s
                            Event Value: %s
                            Event ID: %s
                            -----------------------
                            
                            %n""", eventType,
                    value,
                    eventId);
        }
    }

    public int getSize() {
        return emittedList.size();
    }

    public List getEmittedList() {
        return emittedList;
    }

    public int getAndClearEmittedCount() {
        int count = emittedList.size();
        emittedList.clear();
        return count;
    }

    public void clearEmitted() {
        emittedList.clear();
    }
}
