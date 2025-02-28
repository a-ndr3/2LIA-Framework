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

        String simpleSelect = "@name('my-statement') select * from ComplexEvent where value >= 54.5 and value <= 55.4;";

        String timeWindowForDynamicSelection = """
                @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                @public create window TestWindow#keepall as select * from ComplexEvent;
                insert into TestWindow select * from ComplexEvent;
                """;

        queries.add(new LSSQuery(simpleSelect, staticQueriesDeploymentId));
        queries.add(new LSSQuery(timeWindowForDynamicSelection, runtimeQueriesDeploymentId));

        compileAndDeploy(runtime, configuration, queries);
    }

    public void addMoreQueries() {

        String context = "@public create context ComplexEventContext start @now end after 5 minutes;";
        //@public create context ComplexEventContext initiated @now and pattern [every timer:interval(5 min)] terminated after 5 minutes

        //difference: the first starts when system runs and after 5 minutes, the context ends, and a new one is NOT started (if an event arrives after 5 minutes, it is ignored unless a new context is started)
        //            the second starts when system runs and every 5 minutes a new instance of the context is created (good if check latest 5 minutes of events)


        queries.add(new LSSQuery(context, "contextCreation"));
        //no partitioning -> all events exist in the same space
        //keeps a time-limited session for event processing

        //partitions events by eventType -> each eventType gets its own event processing space
        //String context = "@public create context ComplexEventContext partition by eventType from ComplexEvent;";

        String selectWithinTimeWindow = """
                    @name('selectWithinTimeWindowStatement')
                    context ComplexEventContext
                    select window(*) as events from ComplexEvent(value > 45)#length(2);
                """;
        queries.add(new LSSQuery(selectWithinTimeWindow, "selectWithinTimeWindow"));

        String selectWithinBatchWindow = """
                @name('selectWithinBatchWindowStatement')
                context ComplexEventContext
                select eventId, eventType, value from ComplexEvent.win:time_batch(10 sec) where value > 45;
                """;
        queries.add(new LSSQuery(selectWithinBatchWindow, "selectWithinBatchWindow"));

        String eventChainCheck = """
                    @name('eventChainCheckStatement')
                    select a.eventType as aType, a.eventId as aId, b.eventType as bType, b.eventId as bId, a.value as aValue, b.value as bValue
                    from pattern [
                        every a=ComplexEvent(eventType='Event1', value > 55.0) ->
                        (b=ComplexEvent(eventType='Event2', value = 33.0) where timer:within(10 sec))
                    ];
                """;

        queries.add(new LSSQuery(eventChainCheck, "eventChainCheck"));

        String eventDependenciesCheck = """
                    @name('eventDependenciesCheckStatement')
                    select a.eventId as A_ID, 'ALERT: Event2 did not occur within 10 sec' as alert
                    from pattern [
                        every a=ComplexEvent(eventType='Event1', value > 60.0) ->
                        (timer:interval(10 sec) and not ComplexEvent(eventType='Event2'))
                    ];
                """;
        //if A happens and B doesn't occur in 10 seconds rise an alert
        queries.add(new LSSQuery(eventDependenciesCheck, "eventDependenciesCheck"));

        String twoEventsInOrder = """
                    @name('twoEventsInOrderStatement')
                    select a1.eventId as First_A, a2.eventId as Second_A, b.eventId as B_ID
                    from pattern [
                        every (a1=ComplexEvent(eventType='Event1') ->
                               a2=ComplexEvent(eventType='Event1', a2.eventId != a1.eventId)) ->
                        b=ComplexEvent(eventType='Event2')
                    ];
                """;
        //detects two A events before B occurs
        queries.add(new LSSQuery(twoEventsInOrder, "twoEventsInOrder"));
    }

    public void addLSSQueries() {

        String cascadeImpact = """
                @name('cascadeImpactStatement')
                select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID
                from pattern [
                    every a=ComplexEvent(eventType='SystemA_Update') ->
                    (b=ComplexEvent(eventType='SystemB_Change') and
                     c=ComplexEvent(eventType='SystemC_Change'))
                    where timer:within(10 sec)
                ];
                """;

        queries.add(new LSSQuery(cascadeImpact, "cascadeImpact"));

        String slowDetection = """
                @name('slowdownDetectionStatement')
                select a.eventId as A_ID, 'ALERT: System Response Too Slow' as alert
                from pattern [
                    every a=ComplexEvent(eventType='SystemA_Update') ->
                    (timer:interval(5 sec) and not ComplexEvent(eventType='SystemB_Response'))
                ];
                """;
        queries.add(new LSSQuery(slowDetection, "slowDetection"));

        String conflictingEvents = """
                @name('conflictDetectionStatement')
                select a.eventId as A_ID, b.eventId as B_ID, 'ALERT: SystemB crashed after SystemA update' as alert
                from pattern [
                    every a=ComplexEvent(eventType='SystemA_Update') ->
                    b=ComplexEvent(eventType='SystemB_Down') where timer:within(5 sec)
                ];
                """;
        queries.add(new LSSQuery(conflictingEvents, "conflictingEvents"));

        String eventSpikes = """
                @name('eventSpikesStatement')
                select a.eventId as A_ID, count(*) as errorCount, 'ALERT: System instability detected' as alert
                from ComplexEvent(eventType='SystemError').win:time_batch(20 sec) as b
                join ComplexEvent(eventType='SystemA_Update').win:time(20 sec) as a
                having count(*) > 5;
                """;
        queries.add(new LSSQuery(eventSpikes, "eventSpikes"));
    }

    public void addRemindsConstraintsQueries() {
        String compositeConstraint = """
                @name('compositeConstraintStatement')
                select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID, 'ALERT: Composite Constraint violation' as alert
                from pattern [
                    every (a=ComplexEvent(eventType = 'EventA', value > 100) or
                           b=ComplexEvent(eventType = 'EventB', value < 20) or
                           c=ComplexEvent(eventType = 'EventC', value > 5000))
                ];
                """;

        queries.add(new LSSQuery(compositeConstraint, "compositeConstraint"));

        String multipleDataChecks = """
                @name('multipleDataChecksStatement')
                select st.eventId as Start_ID, temp.eventId as Temp_ID, qual.eventId as Qual_ID, endA.eventId as End_ID,
                       'ALERT: All 3 conditions are true' as alert
                from pattern [
                    every st=ComplexEvent(eventType='Start_Analysis') ->
                    (temp=ComplexEvent(eventType='Temperature', value > 1250) and
                     qual=ComplexEvent(eventType='Quality', value > 0.98) and
                     endA=ComplexEvent(eventType='End_Analysis'))
                    where timer:within(10 sec)
                ];
                """;
        queries.add(new LSSQuery(multipleDataChecks, "multipleDataChecks"));

        //changed 1 hour (original) to 1 minute
        String crossEventDataAccess = """
                @name('crossEventDataStatement')
                select p1.eventId as Point1_ID, p2.eventId as Point2_ID, p3.eventId as Point3_ID,
                       'ALERT: Cross-event temperature sequence' as alert
                from pattern [
                    every p1=ComplexEvent(eventType='LabAnalysis1') ->
                    p2=ComplexEvent(eventType='LabAnalysis2', value < p1.value) ->
                    p3=ComplexEvent(eventType='LabAnalysis3', value < p2.value)
                    where timer:within(1 min)
                ];
                """;
        queries.add(new LSSQuery(crossEventDataAccess, "crossEventData"));

        String multipleDataChecksForOneEvent = """
                @name('multipleDataChecksStatement')
                select 'ALERT: Multiple data checks for one event' as alert
                from ComplexEvent where (eventType = "Percentage" and value > 5 and eventId = 2);
                """;
        queries.add(new LSSQuery(multipleDataChecksForOneEvent, "multipleDataChecksForOneEvent"));

        String eventBasedEvaluationCriteria = """
                @name('eventBasedEvaluationCriteriaStatement')
                select 'ALERT: Sequence not completed before next StartAnalysis' as alert
                from pattern [
                    every a=ComplexEvent(eventType='StartAnalysis') ->
                    (b=ComplexEvent(eventType='TempAnalysis')
                    and c=ComplexEvent(eventType='QualityAnalysis')
                    and d=ComplexEvent(eventType='EndAnalysis'))
                    until nextStart=ComplexEvent(eventType='StartAnalysis')
                ];
                """;
        queries.add(new LSSQuery(eventBasedEvaluationCriteria, "eventBasedEvaluationCriteria"));

        //check if events didn't occur in 8 sec
//        every st=ComplexEvent(eventType='ProductionStarted') ->
//        (timer:interval(8 sec) and not (ComplexEvent(eventType='QualityAnalyzed')
//        and ComplexEvent(eventType='ProductionEnded')))
        String multipleEvaluationCriteria = """
                @name('multipleEvaluationCriteriaStatement')
                select
                a.eventId,
                b[0].eventId,
                c[0].eventId,
                'ALERT: Sequence not completed before next ProductionStart' as alert
                from pattern [
                    every a=ComplexEvent(eventType='ProductionStarted') ->
                    ((b=ComplexEvent(eventType='QualityAnalyzed')
                    and c=ComplexEvent(eventType='ProductionEnded'))
                    where timer:within(8 sec))
                    until nextStart=ComplexEvent(eventType='ProductionStarted')
                ];
                """;
        //changed 8 hours (original) to 8 seconds
        queries.add(new LSSQuery(multipleEvaluationCriteria, "multipleEvaluationCriteria"));


        /* idea =>
        SlagStart must NOT occur within 3 minutes
        TapAnalysis may occur
        TapEnd must occur
        All events can occur in any order within 3 minutes

        changed 3 min to 30 sec
         */
        String flexibleEventSequences = """
                @name('flexibleEventSequencesStatement')
                select st.eventId as TapStart_ID,
                       analysis.eventId as TapAnalysis_ID,
                       endTap.eventId as TapEnd_ID,
                       'ALERT: Flexible event sequence fired' as alert
                from pattern [
                    every st=ComplexEvent(eventType='TapStart') ->
                     (
                     ((not ComplexEvent(eventType='SlagStart') and
                     endTap=ComplexEvent(eventType='TapEnd'))
                     or
                     (not ComplexEvent(eventType='SlagStart') and
                     analysis=ComplexEvent(eventType='TapAnalysis') and
                     endTap=ComplexEvent(eventType='TapEnd'))
                     )
                    where timer:within(30 sec))
                ];
                """;
        queries.add(new LSSQuery(flexibleEventSequences, "flexibleEventSequences"));

        String compositeConstraintGeneralEvents = """
                    @name('compositeConstraintCheckTypeStatement')
                    select 'ALERT: All constraints violated!' as alert
                    from pattern [
                        every (a=EventA(type = true) -> b=EventB(type = true) -> c=EventC(type = true))
                    ];
                """;
        queries.add(new LSSQuery(compositeConstraintGeneralEvents, "compositeConstraintTypeGeneralEvents"));
    }
}
