import TestListeners.*;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventBulkProducer;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.espertech.events.ComplexEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;


import com.espertech.esper.common.client.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ComplexEventsQueriesTests {
    ArrayList<ComplexEvent> events;

    //general tests with complex event
    TestListenerChainTimer testListenerChainTimer;
    TestListenerTimeWindow testListenerTimeWindow;
    TestListenerBatchWindow testListenerBatchWindow;
    TestListenerEventDependencies testListenerDependency;
    TestListenerTwoEventsInOrderBeforeThird testListenerOrder;

    //other test listeners

    TestListenerCascadeEffect testCascadeImpact;
    TestListenerSlowDownDetect testSystemSlowDetect;
    TestListenerConflictingEvents testConflictingEvents;
    TestListenerEventsSpikes testEventSpikes;

    EPRuntime runtime;

    @BeforeEach
    public void setUp() {
        events = new ArrayList<>();
        Configuration config = new Configuration();
        config.getCommon().addEventType(ComplexEvent.class);
        config.getRuntime().getThreading().setInternalTimerEnabled(true);
        runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event3", 44.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event4", 12.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event1", 56.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event2", 60.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event3", 95.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event2", 33.0));
        events.add(KafkaComplexEventBulkProducer.getSpecificEvent("Event5", 1.0));

        var queries = new ComplexEsperQueries();
        queries.addMoreQueries();
        queries.addLSSQueries();

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

        testListenerChainTimer = new TestListenerChainTimer();
        testListenerTimeWindow = new TestListenerTimeWindow();
        testListenerBatchWindow = new TestListenerBatchWindow();
        testListenerDependency = new TestListenerEventDependencies();
        testListenerOrder = new TestListenerTwoEventsInOrderBeforeThird();
        runtime.getDeploymentService().getStatement("eventChainCheck", "eventChainCheckStatement").addListener(testListenerChainTimer);
        runtime.getDeploymentService().getStatement("selectWithinTimeWindow", "selectWithinTimeWindowStatement").addListener(testListenerTimeWindow);
        runtime.getDeploymentService().getStatement("selectWithinBatchWindow", "selectWithinBatchWindowStatement").addListener(testListenerBatchWindow);
        runtime.getDeploymentService().getStatement("eventDependenciesCheck", "eventDependenciesCheckStatement").addListener(testListenerDependency);
        runtime.getDeploymentService().getStatement("twoEventsInOrder", "twoEventsInOrderStatement").addListener(testListenerOrder);


        //LSS queries
        testCascadeImpact = new TestListenerCascadeEffect();
        runtime.getDeploymentService().getStatement("cascadeImpact", "cascadeImpactStatement").addListener(testCascadeImpact);

        testSystemSlowDetect = new TestListenerSlowDownDetect();
        runtime.getDeploymentService().getStatement("slowDetection", "slowdownDetectionStatement").addListener(testSystemSlowDetect);

        testConflictingEvents = new TestListenerConflictingEvents();
        runtime.getDeploymentService().getStatement("conflictingEvents", "conflictDetectionStatement").addListener(testConflictingEvents);

        testEventSpikes = new TestListenerEventsSpikes();
        runtime.getDeploymentService().getStatement("eventSpikes", "eventSpikesStatement").addListener(testEventSpikes);
    }

    private void timer(int seconds) {
        try {
            System.out.println("Advancing Esper time by " + seconds + " seconds...");
            runtime.getEventService().advanceTime(seconds * 1000L);

            ComplexEvent dummyEvent = new ComplexEvent();
            dummyEvent.eventType = "TimeAdvance";
            runtime.getEventService().sendEventBean(dummyEvent, "ComplexEvent");

            System.out.println("Esper time updated. Sleeping for " + seconds + " seconds.");
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testSelectWithinTimeWindow() {
        int counter = 0;

        for (ComplexEvent event : events) {
            System.out.println("Sending event: " + event.getEventType() + " with value: " + event.getValue());
            runtime.getEventService().sendEventBean(event, "ComplexEvent");
            System.out.println("Waiting for 14 seconds");
            timer(14);
            counter++;
            System.out.println("Continue sending events");
            if (counter == 2) {
                Assertions.assertEquals(0, testListenerTimeWindow.emittedList.size());
            }
            if (counter == 4){
                Assertions.assertEquals(2, testListenerTimeWindow.emittedList.size());
                Assertions.assertEquals(56.0, ((ComplexEvent)testListenerTimeWindow.emittedList.get(0)).getValue());
                Assertions.assertEquals(60.0, ((ComplexEvent)testListenerTimeWindow.emittedList.get(1)).getValue());
            }
            if (counter == 5){
                Assertions.assertEquals(2, testListenerTimeWindow.emittedList.size());
                Assertions.assertEquals(60.0, ((ComplexEvent)testListenerTimeWindow.emittedList.get(0)).getValue());
                Assertions.assertEquals(95.0, ((ComplexEvent)testListenerTimeWindow.emittedList.get(1)).getValue());
                break;
            }
        }
    }

    @Test
    public void testSelectWithinBatchWindow() {
        System.out.println("Sending events for batch test");

        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Event1", 56.0), "ComplexEvent");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "Event2", 60.0), "ComplexEvent");
        timer(8);

        Assertions.assertEquals(2, testListenerBatchWindow.emittedList.size());

        timer(1);
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "Event3", 95.0), "ComplexEvent");
        timer(10);

        Assertions.assertEquals(1, testListenerBatchWindow.emittedList.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {9,10})
    public void testEventChainGetEventAfterA(int seconds) {
        System.out.println("Sending events for chain test");
        for (var i = 0; i < events.size(); i++) {
            System.out.println("Current time: " + Instant.ofEpochMilli(runtime.getEventService().getCurrentTime()).atZone(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ISO_LOCAL_TIME));
            runtime.getEventService().sendEventBean(events.get(i), "ComplexEvent");
            if (i == 2)
            {
                timer(seconds);
            }
        }
        if (seconds == 9)
        {
            Assertions.assertEquals(1, testListenerChainTimer.emittedList.size());
            testListenerChainTimer.emittedList.clear();
        }
        else if (seconds == 10)
            Assertions.assertEquals(0, testListenerChainTimer.emittedList.size());
            //won't get event exactly after 10 sec passed
    }

    @Test
    public void testEventChainNoEventAfterA() {
        System.out.println("Sending events for chain test");
        for (var i = 0; i < events.size(); i++) {
            System.out.println("Current time: " + Instant.ofEpochMilli(runtime.getEventService().getCurrentTime()).atZone(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ISO_LOCAL_TIME));
            runtime.getEventService().sendEventBean(events.get(i), "ComplexEvent");
            if (i == 2)
            {
                timer(11);
            }
            Assertions.assertEquals(0, testListenerChainTimer.emittedList.size());
        }
    }

    @Test
    public void testEventDependencies(){
        System.out.println("Sending Event1...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Event1", 65.0), "ComplexEvent");

        timer(12);

        Assertions.assertEquals(1, testListenerDependency.emittedList.size());
    }

    @Test
    public void testTwoEventsBeforeB() {
        System.out.println("Sending Event1...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Event1", 50.0), "ComplexEvent");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "Event1", 55.0), "ComplexEvent");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "Event2", 60.0), "ComplexEvent");

        Assertions.assertEquals(1, testListenerOrder.emittedList.size());

        testListenerOrder.emittedList.clear();
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "Event1", 65.0), "ComplexEvent");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "Event2", 70.0), "ComplexEvent");

        Assertions.assertEquals(0, testListenerOrder.emittedList.size());
    }


    //EXAMPLES: for possible LSS updates analysis

    /**
     * Idea: If system A is updated we check whether system B and system C have changes in X seconds
     */
    @Test
    public void testCascadeEffects() {
        System.out.println("Sending A update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);

        Assertions.assertEquals(0, testCascadeImpact.emittedList.size());

        System.out.println("Sending B change...");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Change", 60.0), "ComplexEvent");

        timer(5);

        Assertions.assertEquals(0, testCascadeImpact.emittedList.size());

        System.out.println("Sending C change...");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemC_Change", 70.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testCascadeImpact.emittedList.size());

        System.out.println("Sending another C change...");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "SystemC_Change", 80.0), "ComplexEvent");
    }


    @Test
    public void testSystemSlowDownAfterUpdate(){
        System.out.println("Sending SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(3); // wait 3 sec (B should still arrive on time)
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Response", 60.0), "ComplexEvent");

        timer(2); // total time = 5 sec
        Assertions.assertEquals(0, testSystemSlowDetect.emittedList.size()); //no alert expected

        // reset the test case for failure scenario
        testSystemSlowDetect.emittedList.clear();
        System.out.println("Sending another SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemA_Update", 50.0), "ComplexEvent");

        timer(6); // wait 6 sec aka slow response
        Assertions.assertEquals(1, testSystemSlowDetect.emittedList.size());
    }

    @Test
    public void testConflictingEvents(){
        System.out.println("Sending SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Down", 60.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(1, testConflictingEvents.emittedList.size());

        testConflictingEvents.emittedList.clear();
        System.out.println("Sending another SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemA_Update", 50.0), "ComplexEvent");

        timer(6);
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "SystemB_Down", 70.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(0, testConflictingEvents.emittedList.size());
    }

    @Test
    public void testEventSpikesAfterUpdate(){
        System.out.println("Sending SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemError", 60.0), "ComplexEvent");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemError", 62.0), "ComplexEvent");

        timer(5);
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "SystemError", 64.0), "ComplexEvent");

        timer(6);
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "SystemError", 66.0), "ComplexEvent");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "SystemError", 68.0), "ComplexEvent");

        timer(5);
        runtime.getEventService().sendEventBean(new ComplexEvent(7, "SystemError", 70.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(1, testEventSpikes.emittedList.size());

        // check behavior if less than 6 errors occur
        testEventSpikes.emittedList.clear();
        System.out.println("Sending another SystemA Update...");
        runtime.getEventService().sendEventBean(new ComplexEvent(8, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(9, "SystemError", 60.0), "ComplexEvent");

        timer(4);
        runtime.getEventService().sendEventBean(new ComplexEvent(10, "SystemError", 62.0), "ComplexEvent");

        timer(6);
        runtime.getEventService().sendEventBean(new ComplexEvent(11, "SystemError", 64.0), "ComplexEvent");

        timer(7);
        runtime.getEventService().sendEventBean(new ComplexEvent(12, "SystemError", 66.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(0, testEventSpikes.emittedList.size());
    }
}