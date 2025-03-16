package com.espertech.ESPERQueries;

import com.espertech.EventTypes.Types.ComplexEvent;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;


public class ComplexEsperQueries extends AbstractQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "complexSelectQueries";
    public static final String runtimeQueriesDeploymentId = "timeWindowQueries";

    public ComplexEsperQueries() {
        configuration = setConfiguration(ComplexEvent.class);
    }

    public void compileEpl(EPRuntime runtime) {

        String simpleSelect = "@name('my-statement') select * from ComplexEvent where value >= 54.5 and value <= 55.4;";

        String timeWindowForDynamicSelection = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select * from ComplexEvent;
                insert into TestWindow select * from ComplexEvent;
                """;

        queries.add(new LSSQuery(simpleSelect, staticQueriesDeploymentId));
        queries.add(new LSSQuery(timeWindowForDynamicSelection, runtimeQueriesDeploymentId));

        compileAndDeploy(runtime, configuration, queries);
    }
}
