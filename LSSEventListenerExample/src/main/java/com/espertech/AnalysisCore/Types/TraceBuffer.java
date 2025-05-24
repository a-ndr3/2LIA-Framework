package com.espertech.AnalysisCore.Types;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TraceBuffer {
    private static TraceBuffer INSTANCE;
    private static final Map<String, TraceContext> activeTraces = new ConcurrentHashMap<>();
    private static final Duration retentionPeriod = Duration.ofMinutes(10);

    public static TraceBuffer getInstance() {
        if (INSTANCE == null) {
            return new TraceBuffer();
        }
        return INSTANCE;
    }

    private TraceBuffer() {

    }

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
