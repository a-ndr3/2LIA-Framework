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
        //for (var event : newEvents) {
            //var temperature = (ComplexEvent) event.getUnderlying();
            //System.out.println("Temperature Alert: \n{" + temperature.getValue() + "\n}");
        //}
        if (newEvents != null) {
            var count = eventCount.addAndGet(newEvents.length);
            var elapsed = System.nanoTime() - startTime;

            System.out.printf("Total Events: %d, Throughput: %.2f events/sec%n",
                    count, (count * 1_000_000_000.0) / elapsed);
        }
    }
}
