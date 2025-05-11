package com.espertech.AnalysisCore.Topology;

import java.util.List;

public interface ITopology {
    List<String> getServicesNamesCalledById(String id);

    List<String> getServicesNamesCallsById(String id);

    List<String> getCalledBy(String id);

    List<String> getCallsBy(String id);

    String getServiceNameById(String id);

    List<String> getServicesNamesCalls(String name);

    List<String> getServicesNamesCalledBy(String name);

    List<String> getServicesCalls(String name);

    List<String> getServicesCalledBy(String name);
}
