package com.espertech;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;

public class KafkaQueries {
    public static EPCompiled compileEpl(Configuration conf) {
        String epl = "@name('my-statement') select * from MySystemEvent where systemId = 'A4' and type = 'update4';\n";

        EPCompiled compiled;
        try {
            compiled = EPCompilerProvider.getCompiler().compile(epl, new CompilerArguments(conf));
        } catch (EPCompileException ex) {
            throw new RuntimeException(ex);
        }

        return compiled;
    }

    public static Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(MySystemEvent.class);
        return configuration;
    }

    public static EPDeployment deploy(EPRuntime runtime, EPCompiled compiled) {
        try {
            return runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId("myEventQueries"));
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }
    }
}
