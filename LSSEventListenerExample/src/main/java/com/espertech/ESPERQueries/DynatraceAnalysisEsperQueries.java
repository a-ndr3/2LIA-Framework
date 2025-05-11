package com.espertech.ESPERQueries;

import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.esper.runtime.client.EPRuntime;

public class DynatraceAnalysisEsperQueries extends AbstractEsperQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "dynatraceSelectQuery";
    public static final String staticQueriesAnotherId = "dynatraceSelectStatusCodeQuery";

    public DynatraceAnalysisEsperQueries() {
        configuration = setConfiguration(DynatraceRecord.class);
    }

    @Override
    public void compileEpl(EPRuntime runtime) {
// INITIAL QUERIES
//        String simpleSelect = "@name('my-statement') select * from DynatraceRecord;";
//
//        String simpleSelectCode = "@name('statusCodeSelect') select * from DynatraceRecord (httpResponseStatusCode != 200);";
//
//        queries.add(new EsperQueryDTO(simpleSelect, staticQueriesDeploymentId));
//
//        queries.add(new EsperQueryDTO(simpleSelectCode, staticQueriesAnotherId));
//
//        compileAndDeploy(runtime, configuration, queries);
    }

}
