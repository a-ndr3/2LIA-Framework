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
    public static void compileEpl(EPRuntime runtime, Configuration conf) {

//        String createContext = "@public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes";
//        String createWindow = "@public create window TestWindow#time(2 minutes) as select systemId, type from MySystemEvent";
//        String script = "insert into TestWindow(systemId,type) select systemId, type from MySystemEvent";

        String selectStatementEPL = "@name('my-statement') select * from MySystemEvent where systemId = 'A1' and type = 'update1';";

//        String epl2 = """
//                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes
//                @public create window TestWindow#time(2 minutes) as select systemId, type from MySystemEvent
//                insert into TestWindow(systemId,type) select systemId, type from MySystemEvent
//                """;

        String epl3 = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select systemId, type from MySystemEvent;
                insert into TestWindow(systemId,type) select systemId, type from MySystemEvent;
                """;
        try {

            //EPCompiled createWindowCompiled = EPCompilerProvider.getCompiler().compile(createContext, new CompilerArguments(conf));
            //EPCompiled insertCompiled = EPCompilerProvider.getCompiler().compile(createWindow, new CompilerArguments(conf));
            //EPCompiled insertIntoCompiled = EPCompilerProvider.getCompiler().compile(script, new CompilerArguments(conf));
            EPCompiled eplCopiled = EPCompilerProvider.getCompiler().compile(epl3, new CompilerArguments(conf));

            EPCompiled selectCompiled = EPCompilerProvider.getCompiler().compile(selectStatementEPL, new CompilerArguments(conf));

            //runtime.getDeploymentService().deploy(createWindowCompiled, new DeploymentOptions().setDeploymentId("myEventQueries1"));
            //runtime.getDeploymentService().deploy(insertCompiled, new DeploymentOptions().setDeploymentId("myEventQueries2"));
            //runtime.getDeploymentService().deploy(insertIntoCompiled, new DeploymentOptions().setDeploymentId("myEventQueries0"));

            runtime.getDeploymentService().deploy(eplCopiled, new DeploymentOptions().setDeploymentId("myEventQueries4"));
            runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId("myEventQueries3"));
        } catch (EPCompileException | EPDeployException ex) {
            throw new RuntimeException(ex);
        }
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
