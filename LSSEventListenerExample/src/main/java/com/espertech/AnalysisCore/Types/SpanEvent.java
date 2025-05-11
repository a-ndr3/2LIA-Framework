package com.espertech.AnalysisCore.Types;

import java.time.Instant;
import java.util.Map;

public abstract class SpanEvent {
    public abstract String getTraceId();
    public abstract String getSpanId();
    public abstract String getServiceId();
    public abstract String getServiceName();
    public abstract String getEndpoint();
    public abstract long getDuration();
    public abstract int getStatusCode();
    public abstract Instant getTime();
}
