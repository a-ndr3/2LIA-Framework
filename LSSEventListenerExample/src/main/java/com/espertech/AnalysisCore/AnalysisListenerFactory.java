package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Topology.TopologyService;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.Main;
import com.espertech.PrometheusMetrics.PrometheusMetrics;
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
            try {
                if (newEvents != null) {
                    for (EventBean e : newEvents) {
                        if (e instanceof MapEventBean meb) {

                            var hashString = meb.getProperties().entrySet().stream()
                                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                                    .sorted()
                                    .reduce("", (a, b) -> a + b).hashCode();

                            if (checkedObjects.containsKey(queryName + hashString)) {
                                // skip already processed events
                            } else {
                                checkedObjects.put(queryName + hashString, e);

                                if (checkTopology && topic == IssueTopicHelper.NETWORK) {

                                    var event = e.getUnderlying();

                                    if (topologyService.isSourceCallsTarget(((HashMap) event).get("origin").toString(), ((HashMap) event).get("affected").toString())) {
                                        PrometheusMetrics.esperQueryMatchCounter
                                                .labels(queryName, IssueTopicHelper.NETWORK.toString())
                                                .inc(1.0);

                                        writeToLog(topic, queryName, meb);
                                    }
                                }
                                else if (!checkTopology && topic == IssueTopicHelper.NETWORK) {
                                    PrometheusMetrics.esperQueryMatchCounter
                                            .labels(queryName, IssueTopicHelper.NETWORK.toString())
                                            .inc(1.0);

                                    writeToLog(topic, queryName, meb);
                                }
                                else if (checkTopology && topic == IssueTopicHelper.ENDPOINT){

                                    var event = e.getUnderlying();

                                    if (!topologyService.isSourceCallsTarget(((HashMap) event).get("origin").toString(), ((HashMap) event).get("affected").toString())) {

                                        PrometheusMetrics.esperQueryMatchCounter
                                                .labels(queryName, IssueTopicHelper.NETWORK.toString())
                                                .inc(1.0);

                                        writeToLog(topic, queryName, meb);
                                    }
                                }
                                else if (!checkTopology && topic == IssueTopicHelper.ENDPOINT) {

                                    PrometheusMetrics.esperQueryMatchCounter
                                            .labels(queryName, IssueTopicHelper.ENDPOINT.toString())
                                            .inc(1.0);

                                    writeToLog(topic, queryName, meb);
                                }
                            }
                        } else if (e instanceof SpanEvent se) {
                            Main.logger.info("Query '{}' fired for Topic: {}", queryName, topic);
                        }
                    }
                }
            } catch (Exception ex) {
                Main.logger.error("Error processing events for query {}: {}", queryName, ex.getMessage(), ex);
                PrometheusMetrics.listenerExceptionsTotal.labels(queryName).inc(1.0);
            }
        };
    }

    private static void writeToLog(IssueTopicHelper topic, String queryName, MapEventBean meb) {
        Main.logger.info("Query {} fired for Topic: {}", queryName, topic);
        for (var property : meb.getProperties().entrySet()) {
            Main.logger.info("      Property: {} = {}", property.getKey(), property.getValue());
        }
    }
}
