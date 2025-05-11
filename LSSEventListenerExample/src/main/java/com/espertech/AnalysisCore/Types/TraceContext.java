package com.espertech.AnalysisCore.Types;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TraceContext {
    public final String traceId;
    private final List<SpanEvent> spanBuffer = new ArrayList<>();
    private Instant lastUpdated;

    public TraceContext(String traceId) {
        this.traceId = traceId;
        this.lastUpdated = Instant.now();
    }

    public void addSpan(SpanEvent event) {
        spanBuffer.add(event);
        lastUpdated = Instant.now();
    }

    public List<SpanEvent> getSpans() {
        return List.copyOf(spanBuffer);
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}
