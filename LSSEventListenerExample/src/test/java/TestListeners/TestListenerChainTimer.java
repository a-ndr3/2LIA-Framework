package TestListeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestListenerChainTimer implements UpdateListener {
    public List<Object> emittedList = new LinkedList<>();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        for (EventBean newEvent : newEvents) {
            emittedList.add(newEvent);
            Map<String, Object> eventMap = (Map<String, Object>) newEvent.getUnderlying();
            System.out.println("Matched Event Chain: A(" + eventMap.get("aType") + ", " + eventMap.get("aValue") + ", " + eventMap.get("aId") +
                    ") → B(" + eventMap.get("bType") + ", " + eventMap.get("bValue") + ", " + eventMap.get("bId") + ")");
        }
    }
}
