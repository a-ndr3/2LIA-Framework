import com.espertech.ESPERQueries.AbstractQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.EventListeners.ComplexEventListener;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventBulkProducer;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.events.ComplexEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;


import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ComplexEventsQueriesTests {
    ArrayList<ComplexEvent> events;
    ComplexEventListener listenerSelectWithinTimeWindow;
    ComplexEventListener listenerSelectWithinBatchWindow;
    ComplexEventListener listenerEventChainCheck;
    ComplexEventListener listenerEventDependenciesCheck;
    ComplexEventListener listenerTwoEventsInOrder;
    EPRuntime runtime;

    @BeforeEach
    public void setUp() {
        events = new ArrayList<>();
        Configuration config = new Configuration();
        config.getCommon().addEventType(ComplexEvent.class);
        config.getRuntime().getThreading().setInternalTimerEnabled(false);
        runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event3", 44.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event4", 12.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event1", 99.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event1", 56.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event3", 99.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event2", 33.0));

        listenerSelectWithinTimeWindow = new ComplexEventListener();
        listenerSelectWithinBatchWindow = new ComplexEventListener();
        listenerEventChainCheck = new ComplexEventListener();
        listenerEventDependenciesCheck = new ComplexEventListener();
        listenerTwoEventsInOrder = new ComplexEventListener();

        var queries = new ComplexEsperQueries();
        queries.addMoreQueries();

        for (var query : queries.queries) {
            EPCompiled selectCompiled = null;
            try {
                var arguments = new CompilerArguments(config);
                arguments.getPath().add(runtime.getRuntimePath());
                selectCompiled = EPCompilerProvider.getCompiler().compile(query.statement, arguments);
                runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId(query.deploymentId));
            } catch (EPDeployException | EPCompileException e) {
                throw new RuntimeException(e);
            }
        }

        runtime.getDeploymentService().getStatement("selectWithinTimeWindow", "selectWithinTimeWindowStatement").addListener(listenerSelectWithinTimeWindow);
        runtime.getDeploymentService().getStatement("selectWithinBatchWindow", "selectWithinBatchWindowStatement").addListener(listenerSelectWithinBatchWindow);
        runtime.getDeploymentService().getStatement("eventChainCheck", "eventChainCheckStatement").addListener(listenerEventChainCheck);
        runtime.getDeploymentService().getStatement("eventDependenciesCheck", "eventDependenciesCheckStatement").addListener(listenerEventDependenciesCheck);
        runtime.getDeploymentService().getStatement("twoEventsInOrder", "twoEventsInOrderStatement").addListener(listenerTwoEventsInOrder);
    }

    @Test
    public void testSelectWithinTimeWindow() throws InterruptedException {
        int counter = 0;

        for (ComplexEvent event : events) {
            System.out.println("Sending event: " + event.getEventName() + " with value: " + event.getValue());
            runtime.getEventService().sendEventBean(event, "ComplexEvent");
            counter++;
            if (counter == 3) {
                break;
            }
            System.out.println("Waiting for 20 seconds");
            for (int i = 0; i < 20; i++) {
                System.out.println("Seconds passed: " + i);
                timer(1);
            }
        }

        Assertions.assertEquals(3, listenerSelectWithinTimeWindow.getAndClearEmittedCount());
    }

    private void timer(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}