package com.espertech.ESPERQueries;

import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.esper.runtime.client.EPRuntime;

public class DynatraceAnalysisEsperQueries extends AbstractEsperQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "dynatraceSelectQuery";

    public DynatraceAnalysisEsperQueries() {
        configuration = setConfiguration(DynatraceRecord.class);
    }

    @Override
    public void compileEpl(EPRuntime runtime) {
        compileAndDeploy(runtime, configuration, queries);
    }

}
