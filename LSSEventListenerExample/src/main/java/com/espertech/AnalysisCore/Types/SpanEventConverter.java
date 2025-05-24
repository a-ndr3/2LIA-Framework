package com.espertech.AnalysisCore.Types;

import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;

public class SpanEventConverter {
    public static SpanEvent convertToSpanEvent(DynatraceRecord record) {
        return new SpanEvent(
                record.endpointName,
                record.serviceId,
                record.duration,
                record.status,
                record.processGroupId,
                record.serviceEntityName,
                record.traceId,
                record.spanId,
                record.httpResponseStatusCode,
                record.startTime
        );
    }
}
