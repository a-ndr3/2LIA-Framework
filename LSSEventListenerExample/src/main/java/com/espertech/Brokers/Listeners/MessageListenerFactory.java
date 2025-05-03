package com.espertech.Brokers.Listeners;

import com.espertech.Brokers.BrokerType;
import com.espertech.ESPERQueries.DynatraceAnalysisEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.Kafka.KafkaListeners.KafkaComplexListener;
import com.espertech.Kafka.KafkaListeners.KafkaDynatraceListener;
import com.espertech.Kafka.config.KafkaEventConfig;

public class MessageListenerFactory {
    public static MessageBrokerListener createListener(BrokerType brokerType, KafkaEventConfig.ConfigType eventClass, String topic, String brokerAddress, LSSEsperQueries esperQueries) {
        switch (brokerType) {
            case BrokerType.KAFKA:
                return switch (eventClass) {
                    case Complex ->
                            new KafkaComplexListener(topic, brokerAddress, esperQueries, ComplexEsperQueries.staticQueriesDeploymentId);
                    case Dynatrace ->
                            new KafkaDynatraceListener(topic, brokerAddress, esperQueries, DynatraceAnalysisEsperQueries.staticQueriesDeploymentId);
                    default -> throw new IllegalArgumentException("Unsupported event class: " + eventClass);
                };
            default:
                throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
    }
}
