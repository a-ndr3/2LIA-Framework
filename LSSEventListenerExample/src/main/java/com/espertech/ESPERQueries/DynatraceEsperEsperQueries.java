package com.espertech.ESPERQueries;

import com.espertech.EventTypes.Types.DynatraceEvent;
import com.espertech.esper.runtime.client.EPRuntime;

public class DynatraceEsperEsperQueries extends AbstractEsperQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "dynatraceSelectQuery";

    public DynatraceEsperEsperQueries() {
        configuration = setConfiguration(DynatraceEvent.class);
    }

    @Override
    public void compileEpl(EPRuntime runtime) {

        String simpleSelect = "@name('my-statement') select * from DynatraceEvent;";

        queries.add(new EsperQueryDTO(simpleSelect, staticQueriesDeploymentId));

        compileAndDeploy(runtime, configuration, queries);
    }

}
