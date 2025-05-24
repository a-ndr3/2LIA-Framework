package com.espertech.AnalysisCore.Topology;

import com.espertech.EventTypes.Types.dynatrace.DynatraceTopology;

import java.util.*;

public class TopologyService implements ITopology {
    private static TopologyService instance;
    private final Map<String, Set<String>> callsGraph;
    private final Map<String, Set<String>> calledByGraph;
    private final Map<String, Set<String>> callsGraphByName;
    private final Map<String, Set<String>> callsNamesByIdGraph;
    private final Map<String, Set<String>> calledByNamesByIdGraph;
    private final Map<String, Set<String>> calledByGraphByName;
    private final Map<String, Set<String>> callsGraphById;
    private final Map<String, Set<String>> calledByGraphById;
    private final Map<String, String> idToNameMap;

    private TopologyService() {
        DynatraceTopology topology = DynatraceTopology.getTopology();

        var topologyRecords = topology.getRecords();

        callsGraph = new HashMap<>();
        callsGraphById = new HashMap<>();
        calledByGraph = new HashMap<>();
        calledByGraphById = new HashMap<>();
        idToNameMap = new HashMap<>();
        callsGraphByName = new HashMap<>();
        callsNamesByIdGraph = new HashMap<>();
        calledByNamesByIdGraph = new HashMap<>();
        calledByGraphByName = new HashMap<>();

        for (var record : topologyRecords) {
            idToNameMap.put(record.getId(), record.getEntityName());
        }

        for (var record : topologyRecords) {
            callsGraph.put(record.getEntityName(), record.getCallsServices() != null ? new HashSet<>(record.getCallsServices()) : null);
            callsGraphById.put(record.getId(), record.getCallsServices() != null ? new HashSet<>(record.getCallsServices()) : null);
            calledByGraph.put(record.getEntityName(), record.getCalledByServices() != null ? new HashSet<>(record.getCalledByServices()) : null);
            calledByGraphById.put(record.getId(), record.getCalledByServices() != null ? new HashSet<>(record.getCalledByServices()) : null);
        }

        for (var record : topologyRecords) {
            var calls = record.getCallsServices();
            if (calls != null) {
                var callsSet = new HashSet<String>();
                for (var call : calls) {
                    callsSet.add(idToNameMap.get(call));
                }
                callsGraphByName.put(record.getEntityName(), callsSet);
                callsNamesByIdGraph.put(record.getId(), callsSet);
            } else {
                callsGraphByName.put(record.getEntityName(), null);
                callsNamesByIdGraph.put(record.getId(), null);
            }
        }

        for (var record : topologyRecords) {
            var calledBy = record.getCalledByServices();
            if (calledBy != null) {
                var calledBySet = new HashSet<String>();
                for (var call : calledBy) {
                    calledBySet.add(idToNameMap.get(call));
                }
                calledByGraphByName.put(record.getEntityName(), calledBySet);
                calledByNamesByIdGraph.put(record.getId(), calledBySet);
            } else {
                calledByGraphByName.put(record.getEntityName(), null);
                calledByNamesByIdGraph.put(record.getId(), null);
            }
        }
    }

    public static TopologyService getInstance() {
        if (instance == null) {
            instance = new TopologyService();
        }
        return instance;
    }

    public List<String> getServicesCalledBy(String serviceName) {
        var calledBy = calledByGraph.get(serviceName);
        if (calledBy == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(calledBy);
    }

    @Override
    public boolean isSourceCallsTarget(String idSource, String idTarget) {
        return instance.getCallsBy(idSource).contains(idTarget);
    }

    public List<String> getServicesCalls(String serviceName) {
        var calls = callsGraph.get(serviceName);
        if (calls == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(calls);
    }

    public List<String> getServicesNamesCalledBy(String serviceName) {
        var calledBy = calledByGraphByName.get(serviceName);
        if (calledBy == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(calledBy);
    }

    public List<String> getServicesNamesCalls(String serviceName) {
        var calls = callsGraphByName.get(serviceName);
        if (calls == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(calls);
    }

    public String getServiceNameById(String id) {
        return idToNameMap.get(id);
    }

    public List<String> getCallsBy(String serviceId) {
        var downstream = callsGraphById.get(serviceId);
        if (downstream == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(downstream);
    }

    public List<String> getCalledBy(String serviceId) {
        var upstream = calledByGraphById.get(serviceId);
        if (upstream == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(upstream);
    }

    public List<String> getServicesNamesCallsById(String serviceId) {
        var downstream = callsNamesByIdGraph.get(serviceId);
        if (downstream == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(downstream);
    }

    public List<String> getServicesNamesCalledById(String serviceId) {
        var upstream = calledByNamesByIdGraph.get(serviceId);
        if (upstream == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(upstream);
    }
}
