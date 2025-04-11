package com.espertech.Brokers.Listeners;

import com.espertech.ESPERQueries.DynatraceEsperEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.Kafka.KafkaListeners.KafkaComplexListener;
import com.espertech.Kafka.KafkaListeners.KafkaDynatraceListener;

public class MessageListenerFactory {
    public static MessageBrokerListener createListener(String brokerType, String eventClass, String topic, String brokerAddress, LSSEsperQueries esperQueries) {
        switch (brokerType.toLowerCase()) {
            case "kafka":
                return switch (eventClass) {
                    case "complexEvents" ->
                            new KafkaComplexListener(topic, brokerAddress, esperQueries, ComplexEsperEsperQueries.staticQueriesDeploymentId);
                    case "dynatraceEvents" ->
                            new KafkaDynatraceListener(topic, brokerAddress, esperQueries, DynatraceEsperEsperQueries.staticQueriesDeploymentId);
                    default -> throw new IllegalArgumentException("Unsupported event class: " + eventClass);
                };
            default:
                throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
    }
}
