package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Types.SpanEvent;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TopicWatcherService {
    public static final String NETWORK_TOPIC = "networkIssues";
    private final AdminClient adminClient;
    private final Pattern topicPattern = Pattern.compile("networkIssues-.*");
    private final Set<String> subscribedTopics = ConcurrentHashMap.newKeySet();

    public TopicWatcherService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public List<String> discoverNewTopics() throws ExecutionException, InterruptedException {
        ListTopicsResult topicsResult = adminClient.listTopics();
        Set<String> allTopics = topicsResult.names().get();
        return allTopics.stream()
                .filter(topicPattern.asPredicate())
                .filter(topic -> !subscribedTopics.contains(topic))
                .collect(Collectors.toList());
    }

    public void startMonitoring(Consumer<SpanEvent> eventHandler) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<String> newTopics = null;
            try {
                newTopics = discoverNewTopics();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (String topic : newTopics) {
                subscribedTopics.add(topic);
                KafkaConsumerService.startListening(topic, eventHandler);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
