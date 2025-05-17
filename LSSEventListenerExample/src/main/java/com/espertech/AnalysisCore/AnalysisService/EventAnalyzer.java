package com.espertech.AnalysisCore.AnalysisService;

import com.espertech.AnalysisCore.Topology.ITopology;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.AnalysisCore.Types.TraceContext;
import com.espertech.EventTypes.LSSEvent;

import java.util.List;

public class EventAnalyzer {
    private final TraceBuffer traceBuffer;
    private final ITopology topology;

    public EventAnalyzer(TraceBuffer traceBuffer, ITopology topology) {
        this.traceBuffer = traceBuffer;
        this.topology = topology;
    }

    public void handle(SpanEvent event) {
        TraceContext ctx = traceBuffer.getOrCreate(event.getTraceId());
        ctx.addSpan(event);

        analyzePropagation(event, ctx);
    }

    private void analyzePropagation(SpanEvent event, TraceContext ctx) {
        String currentService = event.getServiceId();
        List<String> downstreamServices = topology.getCalledBy(currentService);

        for (SpanEvent other : ctx.getSpans()) {
            if (downstreamServices.contains(other.getServiceId()) && other.getStatusCode() >= 400) {
                System.out.printf(
                        "[Propagation Detected] %s ➝ %s for traceId: %s%n",
                        currentService, other.getServiceName(), event.getTraceId()
                );
            }
        }
    }
}
