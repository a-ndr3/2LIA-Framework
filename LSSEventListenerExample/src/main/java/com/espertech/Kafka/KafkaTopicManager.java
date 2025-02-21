package com.espertech.Kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class KafkaTopicManager {
    private AdminClient adminClient = null;

    public KafkaTopicManager(String kafkaBootstrapServers) {
        Properties config = new Properties();
        config.put("bootstrap.servers", kafkaBootstrapServers);
        this.adminClient = AdminClient.create(config);
    }

    public boolean topicExists(String topicName) {
        try {
            Set<String> topics = adminClient.listTopics().names().get();
            return topics.contains(topicName);
        } catch (Exception e) {
            System.err.println("Failed to check if topic exists: " + e.getMessage());
            return false;
        }
    }

    public void createTopicIfNotExists(String topicName) {
        if (!topicExists(topicName)) {
            try {
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("Created Kafka topic: " + topicName);
            } catch (Exception e) {
                System.err.println("Failed to create topic: " + e.getMessage());
            }
        }
    }
}
