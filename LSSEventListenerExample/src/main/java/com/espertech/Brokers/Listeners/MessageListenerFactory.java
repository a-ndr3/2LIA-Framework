package com.espertech.Brokers.Listeners;

import com.espertech.ESPERQueries.DynatraceEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.KafkaListeners.KafkaComplexListener;
import com.espertech.Kafka.KafkaListeners.KafkaDynatraceListener;

public class MessageListenerFactory {
    public static MessageBrokerListener createListener(String brokerType, String eventClass, String topic, String brokerAddress, EsperService esperService, LSSEsperQueries esperQueries) {
        switch (brokerType.toLowerCase()) {
            case "kafka":
                return switch (eventClass) {
                    case "complexEvents" ->
                            new KafkaComplexListener(topic, brokerAddress, esperService, esperQueries, ComplexEsperQueries.staticQueriesDeploymentId);
                    case "dynatraceEvents" ->
                            new KafkaDynatraceListener(topic, brokerAddress, esperService, esperQueries, DynatraceEsperQueries.staticQueriesDeploymentId);
                    default -> throw new IllegalArgumentException("Unsupported event class: " + eventClass);
                };
            default:
                throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
    }
}
