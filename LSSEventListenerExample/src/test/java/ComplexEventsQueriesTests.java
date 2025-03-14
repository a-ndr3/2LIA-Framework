
import RemindsListeners.*;
import TestListeners.*;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.EventGenerators.ComplexEventGenerator;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.espertech.EventTypes.Types.ComplexEvent;
import com.espertech.EventTypes.Types.EventA;
import com.espertech.EventTypes.Types.EventB;
import com.espertech.EventTypes.Types.EventC;
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

    //Reminds listeners
    TestCompositeConstraintListener testListenerComposite;
    TestMultipleDataChecksListener testListenerMultipleData;
    TestCrossEventListener testListenerCrossEvent;
    TestMultipleDataChecksForOneEventListener testListenerMultipleDataForOneEvent;
    TestEventBasedEvaluationCriteria testEventBasedEvaluationCriteria;
    TestMultipleEvaluationListener testMultipleEvaluationListener;
    TestFlexibleEventSequencesListener testFlexibleEventSequencesListener;

    TestCompositeConstraintListener testListenerCompositeTypeDifferentEvents;

    EPRuntime runtime;

    @BeforeEach
    public void setUp() {
        events = new ArrayList<>();
        Configuration config = new Configuration();

        //Config EventTypes
        config.getCommon().addEventType(ComplexEvent.class);

        //DifferentKindEventsTest
        config.getCommon().addEventType(EventA.class);
        config.getCommon().addEventType(EventB.class);
        config.getCommon().addEventType(EventC.class);
        //DifferentKindEventsTest

        config.getRuntime().getThreading().setInternalTimerEnabled(true);

        runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();
        events.add(ComplexEventGenerator.getSpecificEvent("Event3", 44.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event4", 12.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event1", 56.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event2", 60.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event3", 95.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event2", 33.0));
        events.add(ComplexEventGenerator.getSpecificEvent("Event5", 1.0));

        var queries = new ComplexEsperQueries();
        queries.addMoreQueries();
        queries.addLSSQueries();
        queries.addRemindsConstraintsQueries();

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


        //Reminds queries
        testListenerComposite = new TestCompositeConstraintListener();
        runtime.getDeploymentService().getStatement("compositeConstraint", "compositeConstraintStatement").addListener(testListenerComposite);

        testListenerMultipleData = new TestMultipleDataChecksListener();
        runtime.getDeploymentService().getStatement("multipleDataChecks", "multipleDataChecksStatement").addListener(testListenerMultipleData);

        testListenerCrossEvent = new TestCrossEventListener();
        runtime.getDeploymentService().getStatement("crossEventData", "crossEventDataStatement").addListener(testListenerCrossEvent);

        testListenerMultipleDataForOneEvent = new TestMultipleDataChecksForOneEventListener();
        runtime.getDeploymentService().getStatement("multipleDataChecksForOneEvent", "multipleDataChecksStatement").addListener(testListenerMultipleDataForOneEvent);

        testMultipleEvaluationListener = new TestMultipleEvaluationListener();
        runtime.getDeploymentService().getStatement("multipleEvaluationCriteria", "multipleEvaluationCriteriaStatement").addListener(testMultipleEvaluationListener);

        testEventBasedEvaluationCriteria = new TestEventBasedEvaluationCriteria();
        runtime.getDeploymentService().getStatement("eventBasedEvaluationCriteria", "eventBasedEvaluationCriteriaStatement").addListener(testEventBasedEvaluationCriteria);

        testFlexibleEventSequencesListener = new TestFlexibleEventSequencesListener();
        runtime.getDeploymentService().getStatement("flexibleEventSequences", "flexibleEventSequencesStatement").addListener(testFlexibleEventSequencesListener);


        //DifferentKindEventsTest
        testListenerCompositeTypeDifferentEvents = new TestCompositeConstraintListener();
        runtime.getDeploymentService().getStatement("compositeConstraintTypeGeneralEvents", "compositeConstraintCheckTypeStatement").addListener(testListenerCompositeTypeDifferentEvents);
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
            System.out.println("Continue sending EventTypes");
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
        System.out.println("Sending EventTypes for batch test");

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
        System.out.println("Sending EventTypes for chain test");
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
        System.out.println("Sending EventTypes for chain test");
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
        System.out.println("Event1");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Event1", 65.0), "ComplexEvent");

        timer(12);

        Assertions.assertEquals(1, testListenerDependency.emittedList.size());
    }

    @Test
    public void testTwoEventsBeforeB() {
        System.out.println("Event1");
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
        System.out.println("A update");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);

        Assertions.assertEquals(0, testCascadeImpact.emittedList.size());

        System.out.println("B change");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Change", 60.0), "ComplexEvent");

        timer(5);

        Assertions.assertEquals(0, testCascadeImpact.emittedList.size());

        System.out.println("C change");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemC_Change", 70.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testCascadeImpact.emittedList.size());

        System.out.println("another C change");
        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "SystemC_Change", 80.0), "ComplexEvent");
    }


    @Test
    public void testSystemSlowDownAfterUpdate(){
        System.out.println("SystemA Update");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(3);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Response", 60.0), "ComplexEvent");

        timer(2);
        Assertions.assertEquals(0, testSystemSlowDetect.emittedList.size());

        testSystemSlowDetect.emittedList.clear();
        System.out.println("SystemA Update");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemA_Update", 50.0), "ComplexEvent");

        timer(6);
        Assertions.assertEquals(1, testSystemSlowDetect.emittedList.size());
    }

    @Test
    public void testConflictingEvents(){
        System.out.println("SystemA Update");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "SystemA_Update", 50.0), "ComplexEvent");

        timer(2);
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "SystemB_Down", 60.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(1, testConflictingEvents.emittedList.size());

        testConflictingEvents.emittedList.clear();
        System.out.println("SystemA Update");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "SystemA_Update", 50.0), "ComplexEvent");

        timer(6);
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "SystemB_Down", 70.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(0, testConflictingEvents.emittedList.size());
    }

    @Test
    public void testEventSpikesAfterUpdate(){
        System.out.println("SystemA Update");
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
        System.out.println("SystemA Update");
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


    //REMINDS queries tests

    @Test
    public void testCompositeConstraint() {
        System.out.println("Sending event A");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "EventA", 90.0), "ComplexEvent");

        timer(2);

        System.out.println("Sending event B");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "EventB", 30.0), "ComplexEvent");

        timer(2);

        System.out.println("Sending event C");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "EventC", 50.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(0, testListenerComposite.emittedList.size());

        System.out.println("A (violation)");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "EventA", 120.0), "ComplexEvent");

        timer(2);

        System.out.println("B (violation)");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "EventB", 110.0), "ComplexEvent");

        timer(2);

        System.out.println("C (violation)");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "EventC", 120.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testListenerComposite.emittedList.size());
    }

    @Test
    public void testSequenceDataCheck() {
        System.out.println("Start_Analysis event");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Start_Analysis", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("Temperature event");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "Temperature", 1300.0), "ComplexEvent");

        timer(3);

        System.out.println("Quality event");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "Quality", 0.99f), "ComplexEvent");

        timer(3);

        System.out.println("End_Analysis event");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "End_Analysis", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testListenerMultipleData.emittedList.size());

        testListenerMultipleData.emittedList.clear();

        System.out.println("Start_Analysis event");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "Start_Analysis", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("Temperature event");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "Temperature", 1300.0), "ComplexEvent");

        timer(5);

        System.out.println("Quality event (late)");
        runtime.getEventService().sendEventBean(new ComplexEvent(7, "Quality",0.99f), "ComplexEvent");

        timer(4);

        System.out.println("End_Analysis (late)");
        runtime.getEventService().sendEventBean(new ComplexEvent(8, "End_Analysis", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(0, testListenerMultipleData.emittedList.size());
    }

    @Test
    public void testCrossEventDataCheck() {
        System.out.println("LabAnalysis1");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "LabAnalysis1", 1400.0), "ComplexEvent");

        timer(6);

        //added random EventTypes to test if the query is still working
        System.out.println("random event_0");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "RandomEvent", -1.0), "ComplexEvent");

        System.out.println("random event_1");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "RandomEvent", -1.0), "ComplexEvent");


        System.out.println("LabAnalysis2");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "LabAnalysis2", 1350.0), "ComplexEvent");

        timer(9);

        System.out.println("LabAnalysis3");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "LabAnalysis3", 1300.0), "ComplexEvent");

        timer(6);

        Assertions.assertEquals(1, testListenerCrossEvent.emittedList.size());

        testListenerCrossEvent.emittedList.clear();
        System.out.println("Sending another LabAnalysis1...");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "LabAnalysis1", 1400.0), "ComplexEvent");

        timer(6);


        System.out.println("random event_0");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "RandomEvent", -1.0), "ComplexEvent");

        System.out.println("Sending LabAnalysis2 (wrong order)...");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "LabAnalysis2", 1420.0), "ComplexEvent");

        System.out.println("random event_1");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "RandomEvent", -1.0), "ComplexEvent");


        timer(9);


        System.out.println("Sending LabAnalysis3...");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "LabAnalysis3", 1300.0), "ComplexEvent");

        timer(6);

        //no alert expected since LabAnalysis2 had a higher temperature than LabAnalysis1
        Assertions.assertEquals(0, testListenerCrossEvent.emittedList.size());
    }

    @Test
    public void testMultipleDataChecksForOneEvent(){
        System.out.println("Sending event Percentage, no error");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "Percentage", 6.0), "ComplexEvent");

        timer(1);

        System.out.println("Sending event Percentage, no error");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "Percentage", 8.0), "ComplexEvent");

        Assertions.assertEquals(0, testListenerMultipleDataForOneEvent.emittedList.size());

        System.out.println("Percentage (violation)");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "Percentage", 5.5), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testListenerMultipleDataForOneEvent.emittedList.size());
    }

    @Test
    public void testEventBasedEvaluationCriteria(){
        System.out.println("StartAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "StartAnalysis", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("TempAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "TempAnalysis", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("QualityAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "QualityAnalysis", 0.0), "ComplexEvent");

        timer(1);

        System.out.println("EndAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "EndAnalysis", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(0, testEventBasedEvaluationCriteria.emittedList.size());
        testEventBasedEvaluationCriteria.emittedList.clear();

        System.out.println("StartAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "StartAnalysis", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("TempAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "TempAnalysis", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("EndAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(7, "EndAnalysis", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testEventBasedEvaluationCriteria.emittedList.size());

        testEventBasedEvaluationCriteria.emittedList.clear();

        System.out.println("StartAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(8, "StartAnalysis", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("TempAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(9, "TempAnalysis", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("QualityAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(10, "QualityAnalysis", 0.0), "ComplexEvent");

        timer(1);

        System.out.println("StartAnalysis");
        runtime.getEventService().sendEventBean(new ComplexEvent(11, "StartAnalysis", 0.0), "ComplexEvent");

        timer(1);
        Assertions.assertEquals(1, testEventBasedEvaluationCriteria.emittedList.size());
    }

    //TODO: it works but requires FIX => b,c EventTypes return null
    @Test
    public void testMultipleEvaluationCriteria() {
        System.out.println("ProductionStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "ProductionStarted", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("QualityAnalyzed");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "QualityAnalyzed", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("ProductionEnded");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "ProductionEnded", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(0, testMultipleEvaluationListener.emittedList.size());

        testMultipleEvaluationListener.emittedList.clear();
        System.out.println("ProductionStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "ProductionStarted", 0.0), "ComplexEvent");

        timer(2);

        System.out.println("QualityAnalyzed");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "QualityAnalyzed", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("another ProductionStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "ProductionStarted", 0.0), "ComplexEvent");

        timer(1);

        Assertions.assertEquals(1, testMultipleEvaluationListener.emittedList.size());
    }

    @Test
    public void testFlexibleSequences(){
        System.out.println("TapStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(1, "TapStart", 0.0), "ComplexEvent");

        timer(6);

        System.out.println("TapAnalysis (optional)");
        runtime.getEventService().sendEventBean(new ComplexEvent(2, "TapAnalysis", 0.0), "ComplexEvent");

        timer(6);

        System.out.println("TapEnd");
        runtime.getEventService().sendEventBean(new ComplexEvent(3, "TapEnd", 0.0), "ComplexEvent");

        timer(3);

        Assertions.assertEquals(1, testFlexibleEventSequencesListener.emittedList.size());

        testFlexibleEventSequencesListener.emittedList.clear();
        System.out.println("TapStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(4, "TapStart", 0.0), "ComplexEvent");

        timer(3);

        System.out.println("SlagStart");
        runtime.getEventService().sendEventBean(new ComplexEvent(5, "SlagStart", 0.0), "ComplexEvent");

        timer(6);

        System.out.println("TapEnd");
        runtime.getEventService().sendEventBean(new ComplexEvent(6, "TapEnd", 0.0), "ComplexEvent");

        timer(3);

        Assertions.assertEquals(0, testFlexibleEventSequencesListener.emittedList.size());
    }

    @Test
    public void testCompositeConstraintGeneralEvents() {
        System.out.println("EventA");
        runtime.getEventService().sendEventBean(new EventA(true), "EventA");

        timer(2);

        System.out.println("EventB");
        runtime.getEventService().sendEventBean(new EventB(true), "EventB");

        timer(2);

        System.out.println("EventC");
        runtime.getEventService().sendEventBean(new EventC(true), "EventC");

        timer(1);

        Assertions.assertEquals(1, testListenerCompositeTypeDifferentEvents.emittedList.size());

        testListenerCompositeTypeDifferentEvents.emittedList.clear();
        System.out.println("EventA");
        runtime.getEventService().sendEventBean(new EventA(true), "EventA");

        timer(2);

        System.out.println("EventB");
        runtime.getEventService().sendEventBean(new EventB(false), "EventB");

        timer(2);

        System.out.println("EventC");
        runtime.getEventService().sendEventBean(new EventC(true), "EventC");

        timer(1);

        Assertions.assertEquals(0, testListenerCompositeTypeDifferentEvents.emittedList.size());
    }

    @Test
    public void testCompositeConstraintGeneralEventsThroughput(){
        int batchSize = 10_000;
        long startTime = System.nanoTime();

        for (int i = 0; i < batchSize; i++) {
            runtime.getEventService().sendEventBean(new EventA(true), "EventA");
            runtime.getEventService().sendEventBean(new EventB(true), "EventB");
            runtime.getEventService().sendEventBean(new EventC(true), "EventC");
        }

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        double durationSeconds = durationNs / 1_000_000_000.0;

        double throughput = batchSize / durationSeconds;
        System.out.printf("Esper Throughput: %.2f EventTypes/sec%n", throughput);

        Assertions.assertTrue(throughput > 0);
    }

    @Test
    public void testMultipleDataChecksThroughput(){
        int batchSize = 10_000;

        var startEvents = new ArrayList<>();
        var tempEvents = new ArrayList<>();
        var qualEvents = new ArrayList<>();
        var endEvents = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            startEvents.add(ComplexEventGenerator.getComplexEvent(i));
            tempEvents.add(ComplexEventGenerator.getComplexEvent(i));
            qualEvents.add(ComplexEventGenerator.getComplexEvent(i));
            endEvents.add(ComplexEventGenerator.getComplexEvent(i));
        }

        long startTime = System.nanoTime();

        for (int i = 0; i < batchSize; i++) {
            runtime.getEventService().sendEventBean(startEvents.get(i), "ComplexEvent");
            runtime.getEventService().sendEventBean(tempEvents.get(i), "ComplexEvent");
            runtime.getEventService().sendEventBean(qualEvents.get(i), "ComplexEvent");
            runtime.getEventService().sendEventBean(endEvents.get(i), "ComplexEvent");
        }

        long endTime = System.nanoTime();
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;

        double throughput = batchSize / durationSeconds;
        System.out.printf("Esper Throughput for multipleDataChecks: %.2f EventTypes/sec%n", throughput);

        Assertions.assertTrue(throughput > 0);
    }
}