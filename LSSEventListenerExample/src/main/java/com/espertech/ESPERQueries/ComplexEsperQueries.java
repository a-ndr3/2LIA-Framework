package com.espertech.ESPERQueries;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.events.ComplexEvent;


public class ComplexEsperQueries extends AbstractQueries implements LSSEsperQueries {
    public static final String staticQueriesDeploymentId = "complexSelectQueries";
    public static final String runtimeQueriesDeploymentId = "timeWindowQueries";

    public ComplexEsperQueries() {
        configuration = setConfiguration(ComplexEvent.class);
    }

    public void compileEpl(EPRuntime runtime) {

        String simpleSelect = "@name('my-statement') select * from ComplexEvent where value < -40 or value > 60;";

        String timeWindowForDynamicSelection = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select * from ComplexEvent;
                insert into TestWindow select * from ComplexEvent;
                """;

        queries.add(new LSSQuery(simpleSelect, staticQueriesDeploymentId));
        queries.add(new LSSQuery(timeWindowForDynamicSelection, runtimeQueriesDeploymentId));

        compileAndDeploy(runtime, configuration, queries);
    }

    public void addMoreQueries(){
        String context = "@public create context ComplexEventContext partition by eventType from ComplexEvent;";
        //we have 5 event types in the example so there will be 5 partitions
        queries.add(new LSSQuery(context, "contextCreation"));

        String selectWithinTimeWindow = """
                @name('selectWithinTimeWindowStatement')
                context ComplexEventContext
                select eventId, eventName, value from ComplexEvent.win:time(1 min);
                """;
        queries.add(new LSSQuery(selectWithinTimeWindow, "selectWithinTimeWindow"));

        String selectWithinBatchWindow = """
                @name('selectWithinBatchWindowStatement')
                context ComplexEventContext
                select eventId, eventName, value from ComplexEvent.win:time_batch(30 sec);
                """;
        queries.add(new LSSQuery(selectWithinBatchWindow, "selectWithinBatchWindow"));

        String eventChainCheck = """
                @name('eventChainCheckStatement')
                context ComplexEventContext
                select a.eventId as aId, b.eventId as bId, a.eventName as aName, b.eventName as bName
                from pattern [
                every a=ComplexEvent(eventType='Event1' and value > 55.0) -> (b=ComplexEvent(eventType='Event2'))
                ]#time(2 min);
                """;
        //watches for event A with value > 55.0 and then event B of type 'Event2' within 2 minutes
        queries.add(new LSSQuery(eventChainCheck, "eventChainCheck"));

        String eventDependenciesCheck = """
                @name('eventDependenciesCheckStatement')
                context ComplexEventContext
                select a.eventId as A_ID
                from pattern [
                    every a=ComplexEvent(eventType='Event1' and value > 60.0) ->
                    not ComplexEvent(eventType='Event2')
                ]#time(10 sec);
                """;
        //if A happens and B doesn't occur in 10 seconds rise an alert
        queries.add(new LSSQuery(eventDependenciesCheck, "eventDependenciesCheck"));

        String twoEventsInOrder = """
                @name('twoEventsInOrderStatement')
                context ComplexEventContext
                select a1.eventId as First_A, a2.eventId as Second_A, b.eventId as B_ID
                from pattern [
                    every (a1=ComplexEvent(eventType='Event1') ->
                           a2=ComplexEvent(eventType='Event1')) ->
                    b=ComplexEvent(eventType='Event2')
                ];
                """;
        //detects two A events before B occurs
        queries.add(new LSSQuery(twoEventsInOrder, "twoEventsInOrder"));
    }
}
