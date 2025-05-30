package com.espertech.PrometheusMetrics;
import com.espertech.Main;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;

public class PrometheusMetrics {
    public static final int SERVER_PORT = 8083;
    private static HTTPServer server;

    public static final Counter overallEventsPolled = Counter.build()
            .name("polled_events_total")
            .help("Total events polled")
            .register();

    public static final Counter esperEventCounter = Counter.build()
            .name("esper_events_total")
            .help("Total EventTypes processed in Esper")
            .register();

    public static final Counter esperQueryMatchCounter = Counter.build()
            .name("esper_query_matches_total")
            .help("Total times a specific Esper query matched")
            .labelNames("query_name", "issue_type")
            .register();

    public static final Counter analysisQueriesDeployed = Counter.build()
            .name("analysis_queries_deployed")
            .help("Total number of analysis queries deployed in Esper")
            .register();

    public static final Counter analyticsQueriesDeployed = Counter.build()
            .name("analytics_queries_deployed")
            .help("Total number of analytics queries deployed in Esper")
            .register();

    public static final Counter listenerExceptionsTotal = Counter.build()
            .name("listener_exceptions_total")
            .help("Total exceptions thrown in Esper listeners")
            .labelNames("query_name")
            .register();

    public static final Counter queryDeploymentErrorsTotal = Counter.build()
            .name("query_deployment_errors_total")
            .help("Total failed Esper query deployments")
            .labelNames("query_name")
            .register();

    public static void startMetricsServer() {
        try {
            server = new HTTPServer(SERVER_PORT);
            Main.logger.info("Prometheus metrics server started on port {}", SERVER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start Prometheus metrics server", e);
        }
    }

    public static void stopMetricsServer() {
        if (server != null) {
            server.stop();
        }
    }
}
