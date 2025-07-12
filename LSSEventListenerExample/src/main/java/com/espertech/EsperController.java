package com.espertech;

import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.QueriesDatabase.Postgres.PostgresDB;
import com.espertech.QueriesDatabase.QueriesDB;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.fireandforget.EPFireAndForgetQueryResult;
import com.espertech.esper.common.internal.collection.Pair;
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
        try { //todo check correctness
            var deployments = Arrays.stream(esperService.getDeployment().getDeployments()).toList();
            var queries = esperService.getDeployment().getDeployment(deployments.getFirst()).getStatements();
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
    @PostMapping("/addAnalysisQuery")
    public ResponseEntity<String> onAddAnalysisQuery(@RequestBody String epl) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            var rootNode = objectMapper.readTree(epl);
            var actualEpl = rootNode.get("query").asText();
            var category = rootNode.get("category").asText();
            var status = rootNode.get("status").asBoolean();
            var checkTopology = rootNode.get("checkTopology").asBoolean();

            var result = addNewAnalysisQuery(actualEpl, category, status, checkTopology);

            if (result != null) {
                return ResponseEntity.badRequest().body("Error adding analysis query: " + result.getMessage());
            } else {
                return ResponseEntity.ok("Analysis query added successfully");
            }

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
            clearTable();
            db.insertQueries(getTestAnalyticsQueries());
            db.insertAnalysisQueries(getTestAnalysisQueries());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading test events queries to database");
        }
        return ResponseEntity.ok("Dynatrace Test queries uploaded to database");
    }

    @PostMapping("/clearTables")
    public ResponseEntity<String> clearTable() {
        try {
            db.resetTable("esper_queries");
            db.resetTable("analysis_queries");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error clearing table: " + e.getMessage());
        }
        return ResponseEntity.ok("Table cleared successfully");
    }

    @PostMapping("/undeployQuery")
    public ResponseEntity<String> undeployQuery(@RequestBody String queryData) {
        QueryMetadataDTO queryMetaDataDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetaDataDTO = objectMapper.readValue(queryData, QueryMetadataDTO.class);

            var result = esperService.undeployQuery(new EsperQueryDTO(queryMetaDataDTO.query, queryMetaDataDTO.deploymentId, queryMetaDataDTO.queryStatement, queryMetaDataDTO.id));

            if (!result) {
                return ResponseEntity.badRequest().body("Error undeploying query!");
            }

            queryMetaDataDTO.status = false;

            if (queryMetaDataDTO.deploymentId == null || queryMetaDataDTO.deploymentId.isEmpty()){ //analysis query
                db.saveUpdatedAnalysisQuery(queryMetaDataDTO);
            }
            else{ //aggregation query
                db.updateQueryStatus(queryMetaDataDTO.id, false);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error undeploying query: " + e.getMessage());
        }

        return ResponseEntity.ok("Query undeployed successfully");
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
    @PostMapping("/changeAnalysisQuery")
    public ResponseEntity<String> changeAnalysisQuery(@RequestBody String queryData) {
        QueryMetadataDTO queryMetaDataDTO;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            var tmp = objectMapper.readValue(queryData, QueryMetadataDTO.class);

            queryMetaDataDTO = new QueryMetadataDTO(tmp.query, tmp.category, (Objects.equals(tmp.description, "true")), tmp.status);

            if (queryMetaDataDTO.status) {
                var result = esperService.changeExistingAnalysisQuery(new EsperQueryDTO(queryMetaDataDTO.query));

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

    @CrossOrigin(origins = "*")
    @PostMapping("/deployAnalysisQueryFromDB")
    public ResponseEntity<String> deployAnalysisQueryFromDB(@RequestBody String queryData) {
        QueryMetadataDTO queryMetaDataDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetaDataDTO = objectMapper.readValue(queryData, QueryMetadataDTO.class);

            try {
                esperService.deployAnalysisQueries(List.of(queryMetaDataDTO));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error deploying query: " + e.getMessage());
            }

            queryMetaDataDTO.status = true;

            db.saveUpdatedAnalysisQuery(queryMetaDataDTO);
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

    private Exception addNewAnalysisQuery(String epl, String category, Boolean status, Boolean checkTopology) {
        var query = new QueryMetadataDTO(epl, category, status, checkTopology);

        if (status) {
            try {
                esperService.deployAnalysisQueries(List.of(query));
            } catch (Exception e) {
                return e;
            }
        }

        try {
            db.insertAnalysisQueries(List.of(query));
        } catch (Exception e) {
            return e;
        }

        return null;
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

    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST})
    @PostMapping("/checkExistingAnalysisQueriesInDatabase")
    public ResponseEntity<Collection<QueryMetadataDTO>> checkExistingAnalysisQueriesInDatabase() {
        var queries = db.fetchAnalysisQueries();
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

    public static ArrayList<QueryMetadataDTO> getComplexEventsQueries() {
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
                "AnotherSimpleSelect",
                "my-statement2",
                "complexSelectQueries2",
                "@name('my-statement2') select * from ComplexEvent where value > 10 and (eventType = 'Event1' or eventType = 'Event2');",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp1",
                "my-statement2-temp1",
                "complexSelectQueries2-temp1",
                "@name('my-statement2-temp1') select * from ComplexEvent where value < 100 or eventType = 'Event3';",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp2",
                "my-statement2-temp2",
                "complexSelectQueries2-temp2",
                "@name('my-statement2-temp2') select * from ComplexEvent where (eventType = 'Event3' or eventType = 'Event4') and destination = 'Dest2';",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp3",
                "my-statement2-temp3",
                "complexSelectQueries2-temp3",
                "@name('my-statement2-temp3') select * from ComplexEvent where source = 'Source1' and value > 1000;",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp4",
                "my-statement2-temp4",
                "complexSelectQueries2-temp4",
                "@name('my-statement2-temp4') select * from ComplexEvent where source != 'Source1' and destination != 'Dest1';",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp5",
                "my-statement2-temp5",
                "complexSelectQueries2-temp5",
                "@name('my-statement2-temp5') select value from ComplexEvent where source = 'Source3' and destination = 'Dest1';",
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple select query"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "AnotherSimpleSelect-temp6",
                "my-statement2-temp6",
                "complexSelectQueries2-temp6",
                "@name('my-statement2-temp6') select value from ComplexEvent where (source = 'Source3' and destination = 'Dest1') or (eventType='Event3' and priority = 1);",
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
                        @public create context TestContext initiated @now and pattern [every timer:interval(30 sec)] terminated after 2 minutes;
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

//        queries.add(new QueryMetadataDTO(
//                UUID.randomUUID(),
//                "timeWindowForDynamicSelection-temp1",
//                "",
//                "timeWindowQueries-temp1",
//                """
//                        @public create context TestContextCont initiated @now and pattern [every timer:interval(10 sec)] terminated after 3 minutes;
//                        @public create window TestWindow#keepall as select value from ComplexEvent;
//                        insert into TestWindow select value from ComplexEvent;
//                        """,
//                List.of("ComplexEvent"),
//                "System",
//                time,
//                time,
//                false,
//                "Time Window creation"
//        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "context",
                "",
                "contextCreation",
                """
                        @public create context ComplexEventContext start @now end after 2 minutes;
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
                "selectWithinBatchWindow-temp1",
                "selectWithinBatchWindowStatement-temp1",
                "selectWithinBatchWindow-temp1",
                """
                        @name('selectWithinBatchWindowStatement-temp1')
                        context ComplexEventContext
                        select eventId, eventType, source, destination, value from ComplexEvent.win:time_batch(10 sec) where value > 45 and eventType = 'Event2';
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
                "eventChainCheck-temp1",
                "eventChainCheckStatement-temp1",
                "eventChainCheck-temp1",
                """
                            @name('eventChainCheckStatement-temp1')
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
                "compositeConstraint-temp1",
                "compositeConstraintStatement-temp1",
                "compositeConstraint-temp1",
                """
                        @name('compositeConstraintStatement-temp1')
                        select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID, 'ALERT: temp1' as alert
                        from pattern [
                            every (a=ComplexEvent(eventType = 'Event1', value < 100) or
                                   b=ComplexEvent(eventType = 'Event2', priority < 4) or
                                   c=ComplexEvent(eventType = 'Event5', priority > 2))
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
                "compositeConstraint-temp2",
                "compositeConstraintStatement-temp2",
                "compositeConstraint-temp2",
                """
                        @name('compositeConstraintStatement-temp2')
                        select a.eventId as A_ID, b.eventId as B_ID, c.eventId as C_ID, 'ALERT: temp2' as alert
                        from pattern [
                            every (a=ComplexEvent(duration > 2000, value < 100) or
                                   b=ComplexEvent(eventType = 'Event2', priority = 3) or
                                   c=ComplexEvent(eventType = 'Event3', priority = 1))
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
                "crossEventDataAccess-temp1",
                "crossEventDataStatement-temp1",
                "crossEventData-temp1",
                """
                        @name('crossEventDataStatement-temp1')
                        select p1.eventId as Point1_ID, p2.eventId as Point2_ID, p3.eventId as Point3_ID,
                               'ALERT: Cross-event temperature sequence' as alert
                        from pattern [
                            every p1=ComplexEvent(eventType='Event3') ->
                            p2=ComplexEvent(source='Source3', value < p1.value) ->
                            p3=ComplexEvent(eventType='Event3', value < p2.value)
                            where timer:within(10 sec)
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
                "multipleDataChecksForOneEvent-temp1",
                "multipleDataChecksStatement-temp1",
                "multipleDataChecksForOneEvent-temp1",
                """
                        @name('multipleDataChecksStatement-temp1')
                        select 'ALERT: Multiple data checks for one event' as alert
                        from ComplexEvent where (eventType = "Event5" and value > 5 and eventId = 2);
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
                "multipleDataChecksForOneEvent-temp2",
                "multipleDataChecksStatement-temp2",
                "multipleDataChecksForOneEvent-temp2",
                """
                        @name('multipleDataChecksStatement-temp2')
                        select 'ALERT: Multiple data checks for one event' as alert
                        from ComplexEvent where (eventType = "Event1" and source != 'Source1' and eventId > 2);
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
                "multipleDataChecksForOneEvent-temp3",
                "multipleDataChecksStatement-temp3",
                "multipleDataChecksForOneEvent-temp3",
                """
                        @name('multipleDataChecksStatement-temp3')
                        select 'ALERT: Multiple data checks for one event' as alert
                        from ComplexEvent where (eventType = "Event3" and (destination != 'Dest1' and destination != 'Dest2') or eventId > 2);
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
                "eventBasedEvaluationCriteria-temp2",
                "eventBasedEvaluationCriteriaStatement-temp2",
                "eventBasedEvaluationCriteria-temp2",
                """
                        @name('eventBasedEvaluationCriteriaStatement-temp2')
                        select 'ALERT: tempAlert' as alert
                        from pattern [
                            every a=ComplexEvent(source='Source1') ->
                            (b=ComplexEvent(value > 0)
                            and c=ComplexEvent(priority=1)
                            and d=ComplexEvent(duration=1000))
                            until nextStart=ComplexEvent(source='Source1')
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
                "eventBasedEvaluationCriteria-temp1",
                "eventBasedEvaluationCriteriaStatement-temp1",
                "eventBasedEvaluationCriteria-temp1",
                """
                        @name('eventBasedEvaluationCriteriaStatement-temp1')
                        select 'ALERT: tempAlert' as alert
                        from pattern [
                            every a=ComplexEvent(eventType='Event1') ->
                            (b=ComplexEvent(eventType='Event2')
                            and c=ComplexEvent(eventType='Event1')
                            and d=ComplexEvent(eventType='Event3'))
                            until nextStart=ComplexEvent(eventType='Event5')
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
                "multipleEvaluationCriteria-temp2",
                "multipleEvaluationCriteriaStatement-temp2",
                "multipleEvaluationCriteria-temp2",
                """
                        @name('multipleEvaluationCriteriaStatement-temp2')
                        select
                        a.eventId,
                        b[0].eventId,
                        c[0].eventId,
                        'ALERT: tempAlert' as alert
                        from pattern [
                            every a=ComplexEvent(source='Source3') ->
                            ((b=ComplexEvent(source='Source1')
                            and c=ComplexEvent(priority=5))
                            where timer:within(2 sec))
                            until nextStart=ComplexEvent(eventType='Event5')
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
                "multipleEvaluationCriteria-temp1",
                "multipleEvaluationCriteriaStatement-temp1",
                "multipleEvaluationCriteria-temp1",
                """
                        @name('multipleEvaluationCriteriaStatement-temp1')
                        select
                        a.eventId,
                        b[0].eventId,
                        c[0].eventId,
                        'ALERT: tempAlert' as alert
                        from pattern [
                            every a=ComplexEvent(eventType='Event2') ->
                            ((b=ComplexEvent(eventType='Event1')
                            and c=ComplexEvent(eventType='Event3'))
                            where timer:within(8 sec))
                            until nextStart=ComplexEvent(eventType='Event5')
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
                "compositeConstraintGeneralEvents-temp1",
                "compositeConstraintCheckTypeStatement-temp1",
                "compositeConstraintTypeGeneralEvents-temp1",
                """
                            @name('compositeConstraintCheckTypeStatement-temp1')
                            select 'ALERT: temp!' as alert
                            from pattern [
                                every (a=ComplexEvent(eventType = 'Event2') or b=ComplexEvent(source = 'Source1'))
                            ];
                        """,
                List.of("ComplexEvent"),
                "Thresholds",
                time,
                time,
                false,
                "Simple check sequence of events of different types"
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
                                every (a=EventA(type = true) or b=EventB(type = true) or c=EventC(type = true))
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
                "generic",
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
                "generic",
                time,
                time,
                false,
                "Simple select query"
        ));

        return queries;
    }

    private ArrayList<QueryMetadataDTO> getTestAnalyticsQueries() {
        var time = System.currentTimeMillis();
        var queries = new ArrayList<QueryMetadataDTO>();
        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "networkIssues-statusCode",
                "networkIssues-statusCodeSelect404",
                "networkIssues-statusCode",
                "@name('networkIssues-statusCodeSelect404') select * from DynatraceRecord (httpResponseStatusCode = 404);",
                List.of("DynatraceRecord"),
                IssueTopicHelper.NETWORK.toString(),
                time,
                time,
                false,
                "404 status code select query for Dynatrace records"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "endpointsIssues-callChainCheck",
                "endpointsIssue-callChainCheckTest",
                "endpointsIssues-callChainCheck",
                "@name('endpointsIssue-callChainCheckTest') select * from DynatraceRecord (serviceId='SERVICE-53C7C4CB159DE777');",
                List.of("DynatraceRecord"),
                IssueTopicHelper.NETWORK.toString(),
                time,
                time,
                false,
                "Simple call chain check for endpoints"
        ));


        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "endpointsIssues-invalidDirectCallDetection",
                "endpointsIssue-invalidDirectCallDetection",
                "endpointsIssues-invalidDirectCallDetection",
                "@name('endpointsIssue-invalidDirectCallDetection') select * from DynatraceRecord (serviceId='SERVICE-BA02F35BE956EDF9');",
                List.of("DynatraceRecord"),
                IssueTopicHelper.NETWORK.toString(),
                time,
                time,
                false,
                "Direct call detection for endpoints"
        ));

        queries.add(new QueryMetadataDTO(
                UUID.randomUUID(),
                "endpointsIssues-invalidSequenceDetection",
                "endpointsIssue-invalidSequenceDetection",
                "endpointsIssues-invalidSequenceDetection",
                "@name('endpointsIssue-invalidSequenceDetection') select * from DynatraceRecord;",
                List.of("DynatraceRecord"),
                IssueTopicHelper.NETWORK.toString(),
                time,
                time,
                false,
                "Invalid sequence call detection for endpoints"
        ));

        return queries;
    }

    private ArrayList<QueryMetadataDTO> getTestAnalysisQueries() {
        var queries = new ArrayList<QueryMetadataDTO>();

        queries.add(new QueryMetadataDTO(
                """
                        @name('DetectDownstreamErrors')
                        select
                            a.traceId as traceId,
                            a.serviceId as origin,
                            b.serviceId as affected
                        from
                            SpanEvent.win:time_batch(10 sec) as a
                            inner join SpanEvent.win:time_batch(10 sec) as b
                        on
                            a.traceId = b.traceId
                        where
                            a.serviceId != b.serviceId and
                            b.httpResponseStatusCode >= 404;
                        """,
                IssueTopicHelper.NETWORK.toString(),
                true,
                false
        ));

//        queries.add(new QueryMetadataDTO(
//                """
//                                @name('IncorrectCallChain1')
//                                select
//                                    a.endpointName as step1,
//                                    b.endpointName as step2,
//                                    c.endpointName as step3,
//                                    d.endpointName as step4,
//                                    e.endpointName as step5,
//                                    a.serviceId as service
//                                from pattern [
//                                    every (
//                                        a=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        b=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        c=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        d=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        e=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777')
//                                    )
//                                    where timer:within(10 sec)
//                                ]
//                                where (
//                                    a.endpointName = '/api/cart' and
//                                    b.endpointName = '/api/cart/update' and
//                                    c.endpointName = '/api/cart/apply' and
//                                    d.endpointName = '/api/checkout' and
//                                    e.endpointName = '/api/checkout/confirm'
//                                );
//                        """,
//                IssueTopicHelper.ENDPOINT.toString(),
//                false,
//                false
//        ));

//        queries.add(new QueryMetadataDTO(
//                """
//                                @name('IncorrectCallChain')
//                                select
//                                    a.endpointName as step1,
//                                    b.endpointName as step2,
//                                    c.endpointName as step3,
//                                    d.endpointName as step4,
//                                    e.endpointName as step5,
//                                    a.serviceId as service
//                                from pattern [
//                                    every (
//                                        a=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        b=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        c=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        d=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777') ->
//                                        e=SpanEvent(serviceId='SERVICE-53C7C4CB159DE777')
//                                    )
//                                    where timer:within(10 sec)
//                                ]
//                                where not (
//                                    a.endpointName = '/api/cart' and
//                                    b.endpointName = '/api/cart/update' and
//                                    c.endpointName = '/api/cart/apply' and
//                                    d.endpointName = '/api/checkout' and
//                                    e.endpointName = '/api/checkout/confirm'
//                                );
//                        """,
//                IssueTopicHelper.ENDPOINT.toString(),
//                false,
//                false
//        ));
        queries.add(new QueryMetadataDTO(
                """
                        @name('UnexpectedServiceCall')
                        select
                            a.traceId as traceId,
                            a.serviceId as origin,
                            b.serviceId as affected
                        from
                            SpanEvent.win:time_batch(5 sec) as a
                            inner join SpanEvent.win:time_batch(5 sec) as b
                        on
                            a.traceId = b.traceId
                        where
                            a.serviceId != b.serviceId and
                            a.httpResponseStatusCode != b.httpResponseStatusCode;
                        """,
                IssueTopicHelper.ENDPOINT.toString(),
                true,
                false
        ));

        queries.add(new QueryMetadataDTO(
                """
                        @name('InvalidSequenceDetection')
                        select
                            a.traceId as traceId,
                            a.serviceId as serviceA,
                            b.serviceId as serviceB,
                            c.serviceId as serviceC,
                            'ALERT: Invalid call sequence detected A → B → C' as alert
                        from pattern [
                            every (
                                a=SpanEvent(serviceId = 'SERVICE-D8BE5DA6033DDFE5') ->
                                b=SpanEvent(serviceId != 'SERVICE-DEA6B0C4A5ABAABF', traceId=a.traceId) ->
                                c=SpanEvent(serviceId = 'SERVICE-7FFF55032AE71361', traceId=a.traceId)
                            )
                            where timer:within(10 sec)
                        ];
                        """,
                IssueTopicHelper.ENDPOINT.toString(),
                false,
                false
        ));

        return queries;
    }
}
