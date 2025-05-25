package com.espertech.AnalysisCore.AnalysisService;

import com.espertech.AnalysisCore.AnalysisListenerFactory;
import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.SpanEventConverter;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.AnalysisCore.Types.TraceContext;
import com.espertech.EsperService;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.PrometheusMetrics.PrometheusMetrics;
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

            //PrometheusMetrics.esperEventCounter.inc();

            ctx.addSpan(event);
            esperService.getRuntime().getEventService().sendEventBean(event, "SpanEvent");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
