import DynatraceListeners.TestDynatraceListener;
import TestListeners.TestListenerTwoEventsInOrderBeforeThird;
import com.espertech.AnalysisCore.Topology.ITopology;
import com.espertech.AnalysisCore.Topology.TopologyService;
import com.espertech.EventGenerators.DynatraceEventGenerator;
import com.espertech.EventTypes.Types.ComplexEvent;
import com.espertech.EventTypes.Types.dynatrace.DynatraceLog;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.QueriesDatabase.Postgres.PostgresDB;
import com.espertech.QueriesDatabase.QueriesDB;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.*;

public class DynatraceEventsQueriesTests {
    static ArrayList<DynatraceRecord> events;

    static QueriesDB testDb = new PostgresDB();

    static EPRuntime runtime;

    static Collection<QueryMetadataDTO> queriesFromDb;
    static Collection<String> manualQueries = new ArrayList<>();

    Configuration config = new Configuration();

    ITopology topologyService = TopologyService.getInstance();

    TestDynatraceListener testListener = new TestDynatraceListener();

    @BeforeAll
    public static void init() throws IOException {
        //queriesFromDb = testDb.fetchQueries();

        queriesFromDb = new ArrayList<>();

//        manualQueries.add("""
//                create context TraceContext partition by traceId from DynatraceRecord;
//                """);

//        manualQueries.add("""
//                context TraceContext
//                insert into ProblematicTraces
//                select traceId, collect(*) as events
//                from DynatraceRecord(httpResponseStatusCode != 200)
//                output snapshot every 1 minute;
//                """);

//        manualQueries.add("@name('queryTraceId') select * from DynatraceRecord (traceId = '13f1df51a834445a22d69eb58796aa71');");
//        manualQueries.add("@name('queryStatusCode') select * from DynatraceRecord (httpResponseStatusCode != 200);");
//        manualQueries.add("@name('queryDuration') select * from DynatraceRecord (duration > 4000000);");
//        manualQueries.add("""
//                @name('queryTraceChainDetection')
//                select
//                    a.traceId as traceA,
//                    b.traceId as traceB,
//                    c.traceId as traceC
//                from pattern [
//                    every a=DynatraceRecord(traceId='9626086d2807e04daa35916ccf862d3e') ->
//                          b=DynatraceRecord(traceId='dd22ce7051d23495318cbaf9040641ed') ->
//                          c=DynatraceRecord(traceId='7a1fcef2a56ec6af6a47658e1f637b38')
//                ];
//                """);

        events = new ArrayList<>();

        ClassPathResource timeframe1 = new ClassPathResource("traces_spans_astroshop_timeframe1.json");
        //todo: uncomment second timeframe
        //ClassPathResource timeframe2 = new ClassPathResource("traces_spans_astroshop_timeframe2.json");

        var generator = new DynatraceEventGenerator.Builder(timeframe1.getFile()).setGroupByTime(true).build();

        events.addAll(generator.log.records);
    }

    @BeforeEach
    public void setUp() {
        config = new Configuration();

        config.getCommon().addEventType(DynatraceRecord.class);
        config.getCommon().addEventType(DynatraceLog.class);
        config.getCommon().addEventType(ComplexEvent.class);

        config.getRuntime().getThreading().setInternalTimerEnabled(true);

        runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();

        for (var query : queriesFromDb) {
            EPCompiled selectCompiled = null;
            try {
                var arguments = new CompilerArguments(config);
                arguments.getPath().add(runtime.getRuntimePath());
                selectCompiled = EPCompilerProvider.getCompiler().compile(query.query, arguments);
                runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId(query.deploymentId));
            } catch (EPDeployException | EPCompileException e) {
                throw new RuntimeException(e);
            }
        }

        int counter = 0;
        for (var query : manualQueries) {
            EPCompiled selectCompiled = null;
            try {
                var arguments = new CompilerArguments(config);
                arguments.getPath().add(runtime.getRuntimePath());
                selectCompiled = EPCompilerProvider.getCompiler().compile(query, arguments);
                runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId("manualQuery"+counter));
                counter++;
            } catch (EPDeployException | EPCompileException e) {
                throw new RuntimeException(e);
            }
        }

        testListener = new TestDynatraceListener();
        runtime.getDeploymentService().getStatement("manualQuery0", "queryTraceId").addListener(testListener);
        runtime.getDeploymentService().getStatement("manualQuery1", "queryStatusCode").addListener(testListener);
        //runtime.getDeploymentService().getStatement("manualQuery2", "queryDuration").addListener(testListener);
        //runtime.getDeploymentService().getStatement("manualQuery3", "queryTraceChainDetection").addListener(testListener);
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

    private void sendEventsToEsper(int from, int to) {
        System.out.println("Sending events to Esper...");
        for (int i = from; i < to; i++) {
            var event = events.get(i);
            runtime.getEventService().sendEventBean(event, "DynatraceRecord");
        }
    }

    @Test
    public void testLatency() {
        sendEventsToEsper(0,10);

        timer(1);

        sendEventsToEsper(10, 20);

        timer(1);

        sendEventsToEsper(20, 30);

        timer(1);

        sendEventsToEsper(30, 40);

        timer(1);

        sendEventsToEsper(40, 50);

        timer(1);

        sendEventsToEsper(50, 60);

        timer(1);

        sendEventsToEsper(60, 70);

        timer(1);

        sendEventsToEsper(70, events.size());
    }

    @Test
    public void testChainOfServicesCalls(){

    }

    @Test
    public void testSimpleTracePropagation(){

    }

    //        var groupIdMap = new HashMap<String, ArrayList<DynatraceRecord>>();
//        for (var event : events) {
//            if (!groupIdMap.containsKey(event.getTraceId())) {
//                groupIdMap.put(event.getTraceId(), new ArrayList<>());
//            }
//            groupIdMap.get(event.getTraceId()).add(event);
//        }
//
//        for(var entry : groupIdMap.entrySet()){
//            var records = entry.getValue();
//
//            records.sort((o1, o2) -> {
//                if (o1.getStartTime() == null || o2.getStartTime() == null) {
//                    return 0;
//                }
//                return o1.getStartTime().compareTo(o2.getStartTime());
//            });
//        }
}
