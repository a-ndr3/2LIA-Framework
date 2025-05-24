package com.espertech.Brokers.Listeners;

import com.espertech.AnalysisCore.Types.SpanEvent;
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
    protected volatile boolean running = true;

    protected String queriesDeploymentId;

    protected EPRuntime runtime;
    protected EPDeployment deployment;
    protected Configuration configuration;

    public AbstractMessageListener(String topic, LSSEsperQueries esperQueries, String queriesDeploymentId) {
        this.topic = topic;
        this.queriesDeploymentId = queriesDeploymentId;
        try {
            init(EsperServiceImpl.getInstance(), esperQueries, queriesDeploymentId);
        } catch (EPCompileException | EPDeployException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(EsperServiceImpl esperService, LSSEsperQueries esperQueries, String queriesDeploymentId) throws EPCompileException, EPDeployException, RuntimeException {
        configuration = esperQueries.getConfiguration();
        configuration.getCommon().addEventType("SpanEvent", SpanEvent.class);
        //configuration.getCommon().addEventType("com.espertech.AnalysisCore.Types.SpanEvent",
        //        com.espertech.AnalysisCore.Types.SpanEvent.class);

        runtime = EPRuntimeProvider.getRuntime("EventListener", configuration);
        runtime.initialize();

        esperQueries.compileEpl(runtime);
        deployment = runtime.getDeploymentService().getDeployment(queriesDeploymentId);

        esperService.setRuntime(runtime);
        //esperService.setDeployment(deployment);
        esperService.setConfiguration(configuration);

        Main.logger.info("ESPER deployed");
    }
}
