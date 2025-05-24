package com.espertech.AnalysisCore.AnalysisService;

import com.espertech.AnalysisCore.AnalysisListenerFactory;
import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.SpanEventConverter;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.AnalysisCore.Types.TraceContext;
import com.espertech.EsperService;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.esper.common.internal.collection.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EventAnalyzer {
    private final TraceBuffer traceBuffer;
    private final EsperService esperService;

    public EventAnalyzer(TraceBuffer traceBuffer, EsperService esperService) {
        this.traceBuffer = traceBuffer;
        this.esperService = esperService;
    }

    public void handle(Collection<DynatraceRecord> dynatraceRecords) {
        for (DynatraceRecord record : dynatraceRecords) {
            handle(record);
        }
    }

    public void handle(DynatraceRecord dynatraceRecord) {
        try {
            var event = SpanEventConverter.convertToSpanEvent(dynatraceRecord);
            TraceContext ctx = traceBuffer.getOrCreate(event.getTraceId());
            ctx.addSpan(event);
            esperService.getRuntime().getEventService().sendEventBean(event, "SpanEvent");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deployAnalysisQueries(List<Pair<IssueTopicHelper,String>> eplTopicStatement) {
        for (var pair : eplTopicStatement) {
            try {
                var epl = pair.getSecond();
                var topic = pair.getFirst();
                var dto = esperService.deployNewQueryNoDefaultListener(epl, "AnalysisQuery" + epl.length(), List.of("SpanEvent"), "System", "SpanEvent");
                var deployment = esperService.getDeployment().getDeployment(dto.deploymentId);
                Arrays.stream(deployment.getStatements()).findAny()
                        .filter(stmt -> stmt.getName().equals(dto.queryStatement))
                        .ifPresent(stmt -> {
                            stmt.addListener(AnalysisListenerFactory.createListenerForQuery(topic, stmt.getName(), true)); //TODO: add check topology only for queries that need it
                            System.out.printf("Deployed query '%s' for topic %s and attached listener %n", stmt.getName(), topic.toString());
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
