package com.espertech.Brokers.Listeners;

import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.EsperServiceImpl;
import com.espertech.Main;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;

public abstract class AbstractMessageListener implements MessageBrokerListener {
    protected String topic;
    protected EsperService esperService;
    protected volatile boolean running = true;

    protected String queriesDeploymentId;

    protected EPRuntime runtime;
    protected EPDeployment deployment;
    protected Configuration configuration;

    public AbstractMessageListener(String topic, EsperService esperService, LSSEsperQueries esperQueries, String queriesDeploymentId) {
        this.topic = topic;
        this.esperService = esperService;
        this.queriesDeploymentId = queriesDeploymentId;
        try {
            init(esperQueries, queriesDeploymentId);
        } catch (EPCompileException | EPDeployException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(LSSEsperQueries esperQueries, String queriesDeploymentId) throws EPCompileException, EPDeployException, RuntimeException {
        configuration = esperQueries.getConfiguration();

        runtime = EPRuntimeProvider.getRuntime("EventListener", configuration);
        runtime.initialize();

        esperQueries.compileEpl(runtime);
        deployment = runtime.getDeploymentService().getDeployment(queriesDeploymentId);

        ((EsperServiceImpl) this.esperService).setRuntime(runtime);
        ((EsperServiceImpl) this.esperService).setDeployment(deployment);
        ((EsperServiceImpl) this.esperService).setConfiguration(configuration);

        Main.logger.info("ESPER deployed");
    }
}
