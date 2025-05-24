package com.espertech.AnalysisCore;

import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.EventTypes.LSSEvent;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.espertech.esper.common.internal.collection.Pair;
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
    private final AdminClient adminClient;
    private final Set<String> subscribedTopics = ConcurrentHashMap.newKeySet();

    private final List<String> issuePrefixes;
    private final Pattern topicPattern;
    private final Consumer<DynatraceRecord> eventHandler;

    public TopicWatcherService(AdminClient adminClient, Consumer<DynatraceRecord> eventHandler) {
        issuePrefixes = IssueTopicHelper.getProperties();
        topicPattern = Pattern.compile(String.join("|", issuePrefixes) + "-.*");
        this.eventHandler = eventHandler;
        this.adminClient = adminClient;
    }

    public void startMonitoring() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                Set<String> allTopics = adminClient.listTopics().names().get();
                List<String> newTopics = allTopics.stream()
                        .filter(topicPattern.asPredicate())
                        .filter(t -> !subscribedTopics.contains(t))
                        .collect(Collectors.toList());

                for (String topic : newTopics) {
                    subscribedTopics.add(topic);
                    KafkaConsumerService.startListening(topic, eventHandler);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
