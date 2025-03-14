package TestListeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestListenerCascadeEffect implements UpdateListener {
    public List<Object> emittedList = new LinkedList<>();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        for (EventBean newEvent : newEvents) {
            Object result = newEvent.getUnderlying();

            // only store structured query results (not raw EventTypes)
            if (result instanceof Map) {
                emittedList.add(result);
                System.out.println("Query result: " + result);
            }
        }
    }
}