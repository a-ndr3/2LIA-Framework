package com.espertech.ESPERQueries;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.events.ComplexEvent;

public class ComplexEsperQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "complexSelectQueries";
    public static final String runtimeQueriesDeploymentId = "timeWindowQueries";
    public final String deploymentId = "myEventQueries";

    public void compileEpl(EPRuntime runtime, Configuration conf) {

        String simpleSelect = "@name('my-statement') select * from ComplexEvent where value < 20;";

        String timeWindowForDynamicSelection = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select * from ComplexEvent;
                insert into TestWindow(systemId,type) select * from ComplexEvent;
                """;
        try {
            EPCompiled selectCompiled = EPCompilerProvider.getCompiler().compile(simpleSelect, new CompilerArguments(conf));

            EPCompiled timeWindowCompiled = EPCompilerProvider.getCompiler().compile(timeWindowForDynamicSelection, new CompilerArguments(conf));

            runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId(staticQueriesDeploymentId));

            runtime.getDeploymentService().deploy(timeWindowCompiled, new DeploymentOptions().setDeploymentId(runtimeQueriesDeploymentId));

        } catch (EPCompileException | EPDeployException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(ComplexEvent.class);
        return configuration;
    }

    public EPDeployment deploy(EPRuntime runtime, EPCompiled compiled) {
        try {
            return runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(deploymentId));
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getDeploymentId() {
        return deploymentId;
    }
}
