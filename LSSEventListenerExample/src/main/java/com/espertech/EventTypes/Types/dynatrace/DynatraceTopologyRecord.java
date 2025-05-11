package com.espertech.EventTypes.Types.dynatrace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynatraceTopologyRecord {
    @JsonProperty("entity.name")
    private String entityName;

    @JsonProperty("id")
    private String id;

    @JsonProperty("dt.entity.process_group")
    private String processGroup;

    @JsonProperty("dt.entity.process_group_instance")
    private List<String> processGroupInstance;

    @JsonProperty("dt.entity.host")
    private List<String> host;

    @JsonProperty("dt.entity.cloud_application_namespace")
    private List<String> cloudApplicationNamespace;

    @JsonProperty("dt.entity.cloud_application")
    private List<String> cloudApplication;

    @JsonProperty("called_by_services")
    private List<String> calledByServices;

    @JsonProperty("calls_services")
    private List<String> callsServices;

    @JsonProperty("dt.entity.kubernetes_cluster")
    private List<String> kubernetesCluster;

    public String getEntityName() {
        return entityName;
    }

    public List<String> getCallsServices() {
        return callsServices;
    }

    public List<String> getCalledByServices() {
        return calledByServices;
    }

    public String getId() {
        return id;
    }
}
