package com.espertech.AnalysisCore.Types;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TraceBuffer {
    private final Map<String, TraceContext> activeTraces = new ConcurrentHashMap<>();
    private final Duration retentionPeriod = Duration.ofMinutes(10);

    public TraceContext getOrCreate(String traceId) {
        return activeTraces.computeIfAbsent(traceId, TraceContext::new);
    }

    public void cleanupExpired() {
        Instant cutoff = Instant.now().minus(retentionPeriod);
        activeTraces.entrySet().removeIf(e -> e.getValue().getLastUpdated().isBefore(cutoff));
    }

    public Collection<TraceContext> getActiveTraces() {
        return activeTraces.values();
    }
}
