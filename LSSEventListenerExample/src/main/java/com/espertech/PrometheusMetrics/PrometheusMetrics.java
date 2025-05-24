package com.espertech.PrometheusMetrics;
import com.espertech.Main;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;

public class PrometheusMetrics {

    public static final Counter esperEventCounter = Counter.build()
            .name("esper_events_total")
            .help("Total EventTypes processed in Esper")
            .register();

    private static HTTPServer server;

    public static void startMetricsServer() {
        try {
            server = new HTTPServer(8083);
            Main.logger.info("Prometheus metrics server started on port 8083");
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
