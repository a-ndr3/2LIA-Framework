package com.espertech;

import com.espertech.ESPERQueries.EsperQuery;
import com.espertech.ESPERQueries.QueryFactory;
import com.espertech.QueriesDatabase.Postgres.PostgresDB;
import com.espertech.QueriesDatabase.QueriesDB;
import com.espertech.QueriesDatabase.QueryMetadata;
import com.espertech.QueriesDatabase.QueryUploader;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.fireandforget.EPFireAndForgetQueryResult;
import com.espertech.esper.common.client.util.DateTime;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/queries")
@CrossOrigin(origins = "*")
public class EsperController {
    QueriesDB db;

    public EsperController() {
        db = new PostgresDB();
    }

//    @CrossOrigin(origins = "*")
//    @PostMapping("/add")
//    public String addQuery(@RequestBody String epl) {
//        try {
//            String tmp = String.format("@name('my-statement%s') %s;\n", UUID.randomUUID().getMostSignificantBits(), epl);
//            var compiled = EPCompilerProvider.getCompiler().compile(tmp, new CompilerArguments(Main.esperService.getConfiguration()));
//            EPDeployment deployment = Main.esperService.getRuntime().getDeploymentService().deploy(compiled); //todo make service that is responsible for creating correct sql queries
//            return "Query added: " + deployment.getDeploymentId();
//        } catch (Exception e) {
//            return "Failed to add query: " + e.getMessage();
//        }
//    }

    @GetMapping("/getQueries")
    public ResponseEntity<String> getEPLStatements() {
        try {
            var queries = Arrays.stream(Main.esperService.getDeployment().getStatements()).toList(); //todo get service injected
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
    @PostMapping("/onDemandQuery") //todo add sliding window ~ 2 mins
    public ResponseEntity<String> onDemandQuery(@RequestBody String epl) {
        try {
            var runtime = Main.esperService.getRuntime();
            CompilerArguments compilerArguments = new CompilerArguments(Main.esperService.getConfiguration());
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
            QueryUploader qu = new QueryUploader(new PostgresDB());
            for (var query : getComplexEventsQueries()) {
                qu.uploadQuery(query);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading test events queries to database");
        }
        return ResponseEntity.ok("Test queries uploaded to database");
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/changeQuery")
    public ResponseEntity<String> changeQuery(@RequestBody String queryData) {
        QueryMetadata queryMetadata;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetadata = objectMapper.readValue(queryData, QueryMetadata.class);

            if (queryMetadata.status) {
                var result = changeExistingQuery(queryMetadata);

                if (result != null) {
                    return ResponseEntity.badRequest().body("Error changing query: " + result.getMessage());
                }
            }

            updateDB(queryMetadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating query in database: " + e.getMessage());
        }

        return ResponseEntity.ok("Query changed successfully");
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/deployQueryFromDB")
    public ResponseEntity<String> deployQueryFromDB(@RequestBody String queryData){
        QueryMetadata queryMetaData;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new ParameterNamesModule());
            queryMetaData = objectMapper.readValue(queryData, QueryMetadata.class);

            var result = deployQuery(queryMetaData);

            if (result != null) {
                return ResponseEntity.badRequest().body("Error deploying query: " + result.getMessage());
            }

            queryMetaData.status = true;

            updateDB(queryMetaData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deploying query from database: " + e.getMessage());
        }

        return ResponseEntity.ok("Query deployed successfully");
    }

    private Exception deployQuery(QueryMetadata queryData){
        EPCompiled compiled = null;
        var runtime = Main.esperService.getRuntime();
        try {
            CompilerArguments compilerArguments = new CompilerArguments(Main.esperService.getConfiguration());
            compilerArguments.getPath().add(runtime.getRuntimePath());
            compiled = EPCompilerProvider.getCompiler().compile(queryData.query, compilerArguments);
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(queryData.deploymentId));
        } catch (EPCompileException | EPDeployException | IllegalStateException e) {
            return e;
        }
        return null;
    }

    private Exception addNewQuery(String epl, String name, List<String> classes, String category, String description) {
        EPCompiled compiled = null;
        var runtime = Main.esperService.getRuntime();
        try {

            CompilerArguments compilerArguments = new CompilerArguments(Main.esperService.getConfiguration());
            compilerArguments.getPath().add(runtime.getRuntimePath());
            compiled = EPCompilerProvider.getCompiler().compile(epl, compilerArguments);

        } catch (EPCompileException e) {
            return e;
        }

        EsperQuery newQuery = null;
        try {
            newQuery = QueryFactory.getInstance().createQuery(epl);
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(newQuery.deploymentId));

        } catch (EPDeployException | IllegalStateException e) {
            return e;
        }

        try {
            QueryMetadata data = new QueryMetadata(
                    newQuery.id,
                    name,
                    newQuery.queryStatement,
                    newQuery.deploymentId,
                    newQuery.query,
                    classes,
                    category,
                    Instant.now().toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    true,
                    description
            );
            db.insertQuery(data);

        } catch (Exception e) {
            return e;
        }

        return null;
    }

    private void updateDB(QueryMetadata queryData) {
        try {
            db.saveUpdatedQuery(queryData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Exception changeExistingQuery(QueryMetadata queryData) {
        EPCompiled compiled = null;
        var runtime = Main.esperService.getRuntime();
        ArrayList<UpdateListener> existingListeners = new ArrayList<>();
        try {

            try {
                runtime.getDeploymentService().getStatement(queryData.deploymentId, queryData.queryStatement).getUpdateListeners().forEachRemaining(existingListeners::add);
                runtime.getDeploymentService().undeploy(queryData.deploymentId);
            } catch (EPUndeployException | NullPointerException e) {
                return e;
            }

            try {
                CompilerArguments compilerArguments = new CompilerArguments(Main.esperService.getConfiguration());
                compilerArguments.getPath().add(runtime.getRuntimePath());
                compiled = EPCompilerProvider.getCompiler().compileQuery(queryData.query, compilerArguments);
            } catch (EPCompileException e) {
                return e;
            }

            try {
                runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(queryData.deploymentId));

                for (var listener : existingListeners) {
                    runtime.getDeploymentService().getStatement(queryData.deploymentId, queryData.queryStatement).addListener(listener);
                }

            } catch (EPDeployException | IllegalStateException e) {
                return e;
            }

            return null;
        } catch (Exception e) {
            return e;
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST})
    @PostMapping("/checkExistingQueriesInDatabase")
    public ResponseEntity<Collection<QueryMetadata>> checkExistingQueriesInDatabase() {
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

    private ArrayList<QueryMetadata> getComplexEventsQueries() {
        var time = System.currentTimeMillis();
        var queries = new ArrayList<QueryMetadata>();
        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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

        queries.add(new QueryMetadata(
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
}
