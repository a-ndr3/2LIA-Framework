package com.espertech.AnalysisCore.Types;

import com.espertech.EventTypes.LSSEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;

public class SpanEvent implements LSSEvent {
    public OffsetDateTime startTime;

    public String endpointName;

    public String serviceId;

    public Long duration;

    public String status;

    public String processGroupId;

    public String serviceEntityName;

    public String traceId;

    public String spanId;

    public Long httpResponseStatusCode;

    public String esperAnalysisTopic;

    @Override
    public String getClassType() {
        return this.getClass().getSimpleName();
    }

    public SpanEvent() {

    }

    public SpanEvent(String endpointName, String serviceId, Long duration, String status, String processGroupId,
                     String serviceEntityName, String traceId, String spanId, Long httpResponseStatusCode, OffsetDateTime startTime) {
        this.endpointName = endpointName;
        this.serviceId = serviceId;
        this.duration = duration;
        this.status = status;
        this.processGroupId = processGroupId;
        this.serviceEntityName = serviceEntityName;
        this.traceId = traceId;
        this.spanId = spanId;
        this.httpResponseStatusCode = httpResponseStatusCode;
        this.startTime = startTime;
    }

    public Long getHttpResponseStatusCode() {
        return httpResponseStatusCode;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceEntityName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public String getProcessGroupId() {
        return processGroupId;
    }

    public String getStatus() {
        return status;
    }

    public Long getDuration() {
        return duration;
    }

    public String getEsperAnalysisTopic() {
        return esperAnalysisTopic;
    }

    public void setEsperAnalysisTopic(String esperAnalysisTopic) {
        this.esperAnalysisTopic = esperAnalysisTopic;
    }
}
