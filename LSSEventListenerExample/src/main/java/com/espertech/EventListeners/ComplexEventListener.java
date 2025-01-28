package com.espertech.EventListeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.concurrent.atomic.AtomicLong;

public class ComplexEventListener implements UpdateListener {
    private final AtomicLong eventCount = new AtomicLong(0);
    private final long startTime = System.nanoTime();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {

    }
}
