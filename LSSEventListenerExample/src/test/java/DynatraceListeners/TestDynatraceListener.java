package DynatraceListeners;

import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestDynatraceListener implements UpdateListener {
    public final List<Map<String, Object>> emittedList = new LinkedList<>();

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        emittedList.clear();
        for (EventBean newEvent : newEvents) {
            Map<String, Object> resultMap = new HashMap<>();

            for (String property : newEvent.getEventType().getPropertyNames()) {
                try{
                    var pt = newEvent.get(property);
                    resultMap.put(property, pt);
                }
                catch (PropertyAccessException | ClassCastException ex) {
                    resultMap.put(property, null);
                }
            }

            emittedList.add(resultMap);

            if (statement.getName().equals("queryTraceChainDetection"))
                System.out.println("New Event from " + statement.getName() + ": " + resultMap);
        }
    }
}
