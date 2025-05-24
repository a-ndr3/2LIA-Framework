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

public class AnalysisListenerFactory {
    public static TopologyService topologyService = TopologyService.getInstance();

    public static UpdateListener createListenerForQuery(IssueTopicHelper topic, String queryName, boolean checkTopology) {
        return (newEvents, oldEvents, stmt, runtime) -> {
            if (newEvents != null) {
                for (EventBean e : newEvents) {
                    if (e instanceof MapEventBean meb) {
                        if (checkTopology && topic == IssueTopicHelper.NETWORK) {
                            var event = e.getUnderlying();
                            if (topologyService.isSourceCallsTarget(((HashMap) event).get("origin").toString(), ((HashMap) event).get("affected").toString())) {
                                Main.logger.info("Query {} fired for Topic: {} %n", queryName, topic);
                                for (var property : meb.getProperties().entrySet()) {
                                    Main.logger.info("Property: {} = {} %n", property.getKey(), property.getValue());
                                }
                            }
                        }
                        if (topic == IssueTopicHelper.ENDPOINT) {
                            var event = e.getUnderlying();
                        }
                    } else if (e instanceof SpanEvent se) {
                        Main.logger.info("Query '{}' fired for Topic: {} %n", queryName, topic);
                    }
                }
            }
        };
    }
}
