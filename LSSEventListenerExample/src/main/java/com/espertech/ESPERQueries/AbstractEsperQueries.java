package com.espertech.ESPERQueries;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPRuntime;

import java.util.ArrayList;

public abstract class AbstractEsperQueries {
    public Configuration configuration;
    public ArrayList<EsperQueryDTO> queries;

    public AbstractEsperQueries(){
        configuration = new Configuration();
        queries = new ArrayList<>();
    }

    public void compileAndDeploy(EPRuntime runtime, Configuration conf, ArrayList<EsperQueryDTO> queries) {
        for (var query : queries) {
            try {
                EPCompiled selectCompiled = EPCompilerProvider.getCompiler().compile(query.query, new CompilerArguments(conf));
                runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId(query.deploymentId));
            } catch (EPCompileException | EPDeployException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public Configuration setConfiguration(Class<?> eventClass) {
        configuration.getCommon().addEventType(eventClass);
        return configuration;
    }

    public Configuration setConfiguration(Class<?>[] eventClasses) {
        for (Class<?> eventClass : eventClasses) {
            configuration.getCommon().addEventType(eventClass);
        }
        return configuration;

    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
