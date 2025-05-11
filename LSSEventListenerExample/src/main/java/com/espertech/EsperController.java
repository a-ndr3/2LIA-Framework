package com.espertech;

import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.QueriesDatabase.Postgres.PostgresDB;
import com.espertech.QueriesDatabase.QueriesDB;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.fireandforget.EPFireAndForgetQueryResult;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/queries")
@CrossOrigin(origins = "*")
public class EsperController {
    QueriesDB db;
    EsperService esperService;

    public EsperController() {
        db = new PostgresDB();
        esperService = EsperServiceImpl.getInstance();
    }

    @GetMapping("/getQueries")
    public ResponseEntity<String> getEPLStatements() {
        try {
            var queries = Arrays.stream(esperService.getDeployment().getStatements()).toList();
            var sb = new StringBuilder();
            for (var query : queries) {
                sb.append(query.getName()).append("\n");
            }
            return ResponseEntity.ok().body(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get queries: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/onDemandQuery")
    public ResponseEntity<String> onDemandQuery(@RequestBody String epl) {
        try {
            var runtime = esperService.getRuntime();
            CompilerArguments compilerArguments = new CompilerArguments(esperService.getConfiguration());
            compilerArguments.getPath().add(runtime.getRuntimePath());
            var compiled = EPCompilerProvider.getCompiler().compileQuery(epl, compilerArguments);
            EPFireAndForgetQueryResult result = runtime.getFireAndForgetService().executeQuery(compiled);
            var sb = new StringBuilder();
            for (EventBean row : result.getArray()) {
                sb.append((row.getUnderlying()).toString()).append("\n");
            }
            return ResponseEntity.ok().body(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to run query: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/addQueryAuto")
    public ResponseEntity<String> onAddQueryAuto(@RequestBody String epl) {
        try {
            var name = epl.split("@name\\('")[1].split("'")[0];
            ObjectMapper objectMapper = new ObjectMapper();
            var rootNode = objectMapper.readTree(epl);
            String actualEpl = rootNode.get("query").asText();
            return onAddQuery(actualEpl, name, List.of("DynatraceRecord"),
                    "System", "On demand query");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to run query: " + e.getMessage());
        }
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/addQuery")
    public ResponseEntity<String> onAddQuery(@RequestBody String epl,
                                             @RequestParam String name,
                                             @RequestParam List<String> classes,
                                             @RequestParam String category,
                                             @RequestParam String description) {

        var result = addNewQuery(epl, name, classes, category, description);

        if (result != null) {
            return ResponseEntity.badRequest().body("Error adding query: " + result.getMessage());
        }

        return ResponseEntity.ok("Query added successfully");
    }

    @PostMapping("/uploadTestEventsQueries")
    public ResponseEntity<String> uploadComplexEventsQueries() {
        try {
            db.insertQueries(getComplexEventsQueries());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading test events queries to database");
        }
        return ResponseEntity.ok("Test queries uploaded to database");
    }

    @PostMapping("/uploadTestDynatraceQueries")
    public ResponseEntity<String> uploadDynatraceEventQueries() {
        try {
            db.insertQueries(getDynatraceTestQueries());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading test events queries to database");
        }
        return ResponseEntity.ok("Dynatrace Test queries uploaded to database");
    }

    @PostMapping("/clearTable")
    public ResponseEntity<String> clearTable() {
        try {
            db.resetTable("esper_queries");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error clearing table: " + e.getMessage());
        }
        return ResponseEntity.ok("Table cleared successfully");
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/changeQuery")
    public ResponseEntity<String> changeQuery(@RequestBody String queryData) {
        QueryMetadataDTO queryMetaDataDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetaDataDTO = objectMapper.readValue(queryData, QueryMetadataDTO.class);

            if (queryMetaDataDTO.status) {
                var result = esperService.changeExistingQuery(new EsperQueryDTO(queryMetaDataDTO.query, queryMetaDataDTO.deploymentId, queryMetaDataDTO.queryStatement, queryMetaDataDTO.id));

                if (result != null) {
                    return ResponseEntity.badRequest().body("Error changing query: " + result);
                }
            }

            db.saveUpdatedQuery(queryMetaDataDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating query in database: " + e.getMessage());
        }

        return ResponseEntity.ok("Query changed successfully");
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/deployQueryFromDB")
    public ResponseEntity<String> deployQueryFromDB(@RequestBody String queryData) {
        QueryMetadataDTO queryMetaDataDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetaDataDTO = objectMapper.readValue(queryData, QueryMetadataDTO.class);

            var result = esperService.deployQuery(new EsperQueryDTO(queryMetaDataDTO.query, queryMetaDataDTO.deploymentId, queryMetaDataDTO.queryStatement, queryMetaDataDTO.id));

            if (!result.isEmpty()) {
                return ResponseEntity.badRequest().body("Error deploying query: " + result);
            }

            queryMetaDataDTO.status = true;

            db.saveUpdatedQuery(queryMetaDataDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deploying query from database: " + e.getMessage());
        }

        return ResponseEntity.ok("Query deployed successfully");
    }

    private Exception addNewQuery(String epl, String name, List<String> classes, String category, String description) {
        var result = esperService.deployNewQuery(epl, name, classes, category, description);

        if (result == null)
            return new Exception("Failed to deploy query");
        else {
            try {
                db.insertQuery(result);
            } catch (Exception e) {
                return e;
            }

            return null;
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST})
    @PostMapping("/checkExistingQueriesInDatabase")
    public ResponseEntity<Collection<QueryMetadataDTO>> checkExistingQueriesInDatabase() {
        var queries = db.fetchQueries();
        if (queries.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(queries);
    }

    @PostMapping("/pingDB")
    public ResponseEntity<String> pingDB() {
        if (db.ping()) {
            return ResponseEntity.ok("DB is UP");
        }
        return ResponseEntity.status(500).body("DB is DOWN");
    }

    private ArrayList<QueryMetadataDTO> getComplexEventsQueries() {
        var time = System.currentTimeMillis();
        var queries = new ArrayList<QueryMetadataDTO>();
        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "SimpleSelect",
                "my-statement",
                "complexSelectQueries",
                "@name('my-statement') select * from ComplexEvent where value >= 54.5 and value <= 55.4;",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "timeWindowForDynamicSelection",
                "",
                "timeWindowQueries",
                """
                        @public create context TestContext initiated @now and pattern [every timer:interval(2 min)] terminated after 2 minutes;
                        @public create window TestWindow#keepall as select * from ComplexEvent;
                        insert into TestWindow select * from ComplexEvent;
                        """,
                List.of("ComplexEvent"),
                "System",
                time,
                time,
                false,
                "Time Window creation"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "context",
                "",
                "contextCreation",
                """
                        @public create context ComplexEventContext start @now end after 5 minutes;
                        """,
                List.of("ComplexEvent"),
                "System",
                time,
                time,
                false,
                "Context creation query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "selectWithinTimeWindow",
                "selectWithinTimeWindowStatement",
                "selectWithinTimeWindow",
                """
                            @name('selectWithinTimeWindowStatement')
                            context ComplexEventContext
                            select window(*) as events from ComplexEvent(value > 45)#length(2);
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Select within time window"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "selectWithinBatchWindow",
                "selectWithinBatchWindowStatement",
                "selectWithinBatchWindow",
                """
                        @name('selectWithinBatchWindowStatement')
                        context ComplexEventContext
                        select eventId, eventType, value from ComplexEvent.win:time_batch(10 sec) where value > 45;
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Select within batch time window"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "eventChainCheck",
                "eventChainCheckStatement",
                "eventChainCheck",
                """
                            @name('eventChainCheckStatement')
                            select a.eventType as aType, a.eventId as aId, b.eventType as bType, b.eventId as bId, a.value as aValue, b.value as bValue
                            from pattern [
                                every a=ComplexEvent(eventType='Event1', value > 55.0) ->
                                (b=ComplexEvent(eventType='Event2', value = 33.0) where timer:within(10 sec))
                            ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check event chain within 10 seconds"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "eventDependenciesCheck",
                "eventDependenciesCheckStatement",
                "eventDependenciesCheck",
                """
                            @name('eventDependenciesCheckStatement')
                            select a.eventId as A_ID, 'ALERT: Event2 did not occur within 10 sec' as alert
                            from pattern [
                                every a=ComplexEvent(eventType='Event1', value > 60.0) ->
                                (timer:interval(10 sec) and not ComplexEvent(eventType='Event2'))
                            ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check events dependencies"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "twoEventsInOrder",
                "twoEventsInOrderStatement",
                "twoEventsInOrder",
                """
                            @name('twoEventsInOrderStatement')
                            select a1.eventId as First_A, a2.eventId as Second_A, b.eventId as B_ID
                            from pattern [
                                every (a1=ComplexEvent(eventType='Event1') ->
                                       a2=ComplexEvent(eventType='Event1', a2.eventId != a1.eventId)) ->
                                b=ComplexEvent(eventType='Event2')
                            ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check that events occur in order"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "cascadeImpact",
                "cascadeImpactStatement",
                "cascadeImpact",
                """
                        @name('cascadeImpactStatement')
                        select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID
                        from pattern [
                            every a=ComplexEvent(eventType='SystemA_Update') ->
                            (b=ComplexEvent(eventType='SystemB_Change') and
                             c=ComplexEvent(eventType='SystemC_Change'))
                            where timer:within(10 sec)
                        ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check cascade appearance of events"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "slowDetection",
                "slowdownDetectionStatement",
                "slowDetection",
                """
                        @name('slowdownDetectionStatement')
                        select a.eventId as A_ID, 'ALERT: System Response Too Slow' as alert
                        from pattern [
                            every a=ComplexEvent(eventType='SystemA_Update') ->
                            (timer:interval(5 sec) and not ComplexEvent(eventType='SystemB_Response'))
                        ];
                        """,
                List.of("ComplexEvent"),
                "Performance Check",
                time,
                time,
                false,
                "Check system response time"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "conflictingEvents",
                "conflictDetectionStatement",
                "conflictingEvents",
                """
                        @name('conflictDetectionStatement')
                        select a.eventId as A_ID, b.eventId as B_ID, 'ALERT: SystemB crashed after SystemA update' as alert
                        from pattern [
                            every a=ComplexEvent(eventType='SystemA_Update') ->
                            b=ComplexEvent(eventType='SystemB_Down') where timer:within(5 sec)
                        ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check chain of events"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "eventSpikes",
                "eventSpikesStatement",
                "eventSpikes",
                """
                        @name('eventSpikesStatement')
                        select a.eventId as A_ID, count(*) as errorCount, 'ALERT: System instability detected' as alert
                        from ComplexEvent(eventType='SystemError').win:time_batch(20 sec) as b
                        join ComplexEvent(eventType='SystemA_Update').win:time(20 sec) as a
                        having count(*) > 5;
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check event spikes after a particular event"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "compositeConstraint",
                "compositeConstraintStatement",
                "compositeConstraint",
                """
                        @name('compositeConstraintStatement')
                        select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID, 'ALERT: Composite Constraint violation' as alert
                        from pattern [
                            every (a=ComplexEvent(eventType = 'EventA', value > 100) or
                                   b=ComplexEvent(eventType = 'EventB', value < 20) or
                                   c=ComplexEvent(eventType = 'EventC', value > 5000))
                        ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check composite constraint where any of the events can trigger the alert"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "multipleDataChecks",
                "multipleDataChecksStatement",
                "multipleDataChecks",
                """
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
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check multiple conditions in a single query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "crossEventDataAccess",
                "crossEventDataStatement",
                "crossEventData",
                """
                        @name('crossEventDataStatement')
                        select p1.eventId as Point1_ID, p2.eventId as Point2_ID, p3.eventId as Point3_ID,
                               'ALERT: Cross-event temperature sequence' as alert
                        from pattern [
                            every p1=ComplexEvent(eventType='LabAnalysis1') ->
                            p2=ComplexEvent(eventType='LabAnalysis2', value < p1.value) ->
                            p3=ComplexEvent(eventType='LabAnalysis3', value < p2.value)
                            where timer:within(1 min)
                        ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check cross event data access and comparison"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "multipleDataChecksForOneEvent",
                "multipleDataChecksStatement",
                "multipleDataChecksForOneEvent",
                """
                        @name('multipleDataChecksStatement')
                        select 'ALERT: Multiple data checks for one event' as alert
                        from ComplexEvent where (eventType = "Percentage" and value > 5 and eventId = 2);
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check multiple conditions for a single event"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "eventBasedEvaluationCriteria",
                "eventBasedEvaluationCriteriaStatement",
                "eventBasedEvaluationCriteria",
                """
                        @name('eventBasedEvaluationCriteriaStatement')
                        select 'ALERT: Sequence not completed before next StartAnalysis' as alert
                        from pattern [
                            every a=ComplexEvent(eventType='StartAnalysis') ->
                            (b=ComplexEvent(eventType='TempAnalysis')
                            and c=ComplexEvent(eventType='QualityAnalysis')
                            and d=ComplexEvent(eventType='EndAnalysis'))
                            until nextStart=ComplexEvent(eventType='StartAnalysis')
                        ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check event sequence completion before next event"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "multipleEvaluationCriteria",
                "multipleEvaluationCriteriaStatement",
                "multipleEvaluationCriteria",
                """
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
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check multiple evaluation criteria for a sequence of events"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "flexibleEventSequences",
                "flexibleEventSequencesStatement",
                "flexibleEventSequences",
                """
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
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Check several event sequences"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "compositeConstraintGeneralEvents",
                "compositeConstraintCheckTypeStatement",
                "compositeConstraintTypeGeneralEvents",
                """
                            @name('compositeConstraintCheckTypeStatement')
                            select 'ALERT: All constraints violated!' as alert
                            from pattern [
                                every (a=EventA(type = true) -> b=EventB(type = true) -> c=EventC(type = true))
                            ];
                        """,
                List.of("EventA, EventB, EventC"),
                "Thresholds",
                time,
                time,
                false,
                "Simple check sequence of events of different types"
        ));

        return queries;
    }

    private ArrayList<QueryMetadataDTO> getDynatraceTestQueries() {
        var time = System.currentTimeMillis();
        var queries = new ArrayList<QueryMetadataDTO>();
        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "queryTraceId",
                "queryTraceIdStatement",
                "queryTraceId",
                "@name('queryTraceIdStatement') select * from DynatraceRecord (traceId = '13f1df51a834445a22d69eb58796aa71');",
                List.of("DynatraceRecord"),
                "System",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "queryStatusCode",
                "queryStatusCodeStatement",
                "queryStatusCode",
                "@name('queryStatusCodeStatement') select * from DynatraceRecord (httpResponseStatusCode != 200);",
                List.of("DynatraceRecord"),
                "System",
                time,
                time,
                false,
                "Simple select query"
        ));

        return queries;
    }
}
