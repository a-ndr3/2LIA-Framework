package NotUsed;

import com.espertech.ESPERQueries.AbstractQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.ESPERQueries.LSSQuery;
import com.espertech.esper.runtime.client.EPRuntime;


public class SimpleEsperQueries extends AbstractQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "simpleSelectQueries";
    public static final String runtimeQueriesDeploymentId = "timeWindowQueries";

    public SimpleEsperQueries() {
        configuration = setConfiguration(MySystemEvent.class);
    }

    public void compileEpl(EPRuntime runtime) {

        String simpleSelect = "@name('my-statement') select * from MySystemEvent where systemId = 'A1' and type = 'update1';";

        String timeWindowForDynamicSelection = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select systemId, type from MySystemEvent;
                insert into TestWindow(systemId,type) select systemId, type from MySystemEvent;
                """;

        queries.add(new LSSQuery(simpleSelect, staticQueriesDeploymentId));
        queries.add(new LSSQuery(timeWindowForDynamicSelection, runtimeQueriesDeploymentId));

        compileAndDeploy(runtime, configuration, queries);
    }
}
