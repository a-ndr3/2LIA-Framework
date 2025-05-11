package com.espertech.EventTypes.Types.dynatrace;

import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.EventTypes.LSSEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynatraceRecord extends SpanEvent implements LSSEvent {
    @JsonProperty("start_time")
    public OffsetDateTime startTime;

    @JsonProperty("endpoint.name")
    public String endpointName;

    @JsonProperty("dt.entity.service")
    public String serviceId;

    @JsonProperty("duration")
    public Long duration;

    @JsonProperty("request.status_code")
    public String status;

    @JsonProperty("dt.entity.process_group")
    public String processGroupId;

    @JsonProperty("k8s.workload.name")
    public String workloadName;

    @JsonProperty("dt.entity.cloud_application")
    public String cloudApplication;

    @JsonProperty("k8s.namespace.name")
    public String namespaceName;

    @JsonProperty("dt.entity.cloud_application_namespace")
    public String cloudApplicationNamespace;

    @JsonProperty("dt.agent.module.id")
    public String agentModuleId;

    @JsonProperty("dt.entity.service.entity.name")
    public String serviceEntityName;

    @JsonProperty("dt.entity.process_group.entity.name")
    public String processGroupEntityName;

    @JsonProperty("trace.id")
    public String traceId;

    @JsonProperty("span.id")
    public String spanId;

    @JsonProperty("span.source")
    public String spanSource;

    @JsonProperty("http.response.status_code")
    public Long httpResponseStatusCode;

    @JsonProperty("dt.system.sampling_ratio")
    public String samplingRatio;

    @Override
    public String getClassType() {
        return this.getClass().getSimpleName();
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

    @Override
    public String getSpanId() {
        return spanId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceEntityName;
    }

    @Override
    public String getEndpoint() {
        return endpointName;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public int getStatusCode() {
         return httpResponseStatusCode != null ? Math.toIntExact(httpResponseStatusCode) : 0;
    }

    @Override
    public Instant getTime() {
        return startTime.toInstant();
    }
}
