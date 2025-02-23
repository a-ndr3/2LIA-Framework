package RemindsListeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.espertech.events.ComplexEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TestCompositeConstraintListener implements UpdateListener {
    public List<Object> emittedList = new LinkedList<>();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        emittedList.clear();
        for (EventBean newEvent : newEvents) {
            var event = ((HashMap) newEvent.getUnderlying());
            emittedList.add(event);
        }
    }
}