import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esper.runtime.client.EPStatement;

public class TemperatureTrackerTest {
    public static void main(String[] args) {
        new TemperatureTrackerTest().run();
    }

    public void run(){
        Configuration configuration = TemperatureEsperQueries.getConfiguration();
        EPCompiled compiled = TemperatureEsperQueries.compileEPL(configuration);

        System.out.println("Setting up runtime");
        EPRuntime runtime = EPRuntimeProvider.getRuntime("TemperatureChecker", configuration);
        runtime.initialize();

        System.out.println("Deploying compiled EPL");
        TemperatureEsperQueries.deploy(runtime, compiled);

        var listener = new TemperatureAlertListener();
        runtime.getDeploymentService().getStatement("temperatureQueries", "alert").addListener(listener);

        var testGen = new TemperatureEventGenerator().makeEventStream(10000);

        System.out.println("Sending " + testGen.size() + " temperature events");

        for (var event : testGen){
            //System.out.println("Sending event: " + event);
            runtime.getEventService().sendEventBean(event, event.getClass().getSimpleName());
        }

        System.out.println("Done.");
    }
}
