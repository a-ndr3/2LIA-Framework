package com.espertech.PrometheusMetrics;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;

public class PrometheusMetrics {

    public static final Counter esperEventCounter = Counter.build()
            .name("esper_events_total")
            .help("Total EventTypes processed in Esper")
            .register();

    public static final Counter esperAlertCounter = Counter.build()
            .name("esper_anomalies_total")
            .help("Total anomalies detected by Esper")
            .register();

    public static final Gauge eventBufferSize = Gauge.build()
            .name("esper_event_buffer_size")
            .help("Number of EventTypes currently in the buffer waiting to be sent to Kafka")
            .register();

    private static HTTPServer server;

    public static void startMetricsServer() {
        try {
            server = new HTTPServer(8083);
            System.out.println("Prometheus metrics server started on port 8083");
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
