package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Topology.TopologyService;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.Main;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.event.map.MapEventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysisListenerFactory {
    public static TopologyService topologyService = TopologyService.getInstance();
    public static ConcurrentHashMap<String, Object> checkedObjects = new ConcurrentHashMap<>();

    public static UpdateListener createListenerForQuery(IssueTopicHelper topic, String queryName, boolean checkTopology) {
        return (newEvents, oldEvents, stmt, runtime) -> {
            if (newEvents != null) {
                for (EventBean e : newEvents) {
                    if (e instanceof MapEventBean meb) {

                        //calculate hashString using all properties
                        var hashString = meb.getProperties().entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + entry.getValue())
                                .sorted()
                                .reduce("", (a, b) -> a + b).hashCode();

                        if (checkedObjects.containsKey(queryName + hashString)) {
                            continue; // skip already processed events
                        }
                        checkedObjects.put(queryName + hashString, e);

                        if (checkTopology && topic == IssueTopicHelper.NETWORK) {
                            var event = e.getUnderlying();
                            if (topologyService.isSourceCallsTarget(((HashMap) event).get("origin").toString(), ((HashMap) event).get("affected").toString())) {
                                Main.logger.info("Query {} fired for Topic: {}", queryName, topic);
                                for (var property : meb.getProperties().entrySet()) {
                                    Main.logger.info("      Property: {} = {}", property.getKey(), property.getValue());
                                }
                            }
                        }
                        if (topic == IssueTopicHelper.ENDPOINT) {
                            Main.logger.info("Query {} fired for Topic: {}", queryName, topic);
                            for (var property : meb.getProperties().entrySet()) {
                                Main.logger.info("      Property: {} = {}", property.getKey(), property.getValue());
                            }
                        }
                    } else if (e instanceof SpanEvent se) {
                        Main.logger.info("Query '{}' fired for Topic: {}", queryName, topic);
                    }
                }
            }
        };
    }
}
