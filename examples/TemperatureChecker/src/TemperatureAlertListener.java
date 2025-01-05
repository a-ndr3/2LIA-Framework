import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TemperatureAlertListener implements UpdateListener  {
    private List<Object> matchEvents = Collections.synchronizedList(new LinkedList<Object>());

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        for (var event : newEvents){
            //var temperature = (Temperature) event.get("t"); //if using pattern
            var temperature = (Temperature) event.getUnderlying();
            //var observed = event.get("observed");
            System.out.println("Temperature Alert: \n{" + temperature + "\n}"); //"\n observed: " + observed.toString() +
            matchEvents.add(temperature);
        }
    }
}
