package com.espertech.Brokers.Listeners;

import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.Kafka.KafkaListeners.KafkaListener;

public class MessageListenerFactory {
    public static MessageBrokerListener createListener(String brokerType, String topicOrQueue, String brokerAddress, EsperService esperService, LSSEsperQueries esperQueries, String queriesDeploymentId) {
        switch (brokerType.toLowerCase()) {
            case "kafka":
                return new KafkaListener(topicOrQueue, brokerAddress, esperService, esperQueries, queriesDeploymentId);
            default:
                throw new IllegalArgumentException("Unsupported broker type: " + brokerType);
        }
    }
}
