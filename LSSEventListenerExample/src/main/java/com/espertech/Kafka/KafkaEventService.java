package com.espertech.Kafka;

import com.espertech.AnalysisCore.AnalysisService.EventAnalyzer;
import com.espertech.AnalysisCore.TopicWatcherService;
import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.AnalysisCore.Types.TraceBuffer;
import com.espertech.Brokers.BrokerType;
import com.espertech.Brokers.Listeners.MessageBrokerListener;
import com.espertech.Brokers.Listeners.MessageListenerFactory;
import com.espertech.Brokers.Producers.MessageBrokerProducer;
import com.espertech.Brokers.Producers.MessageProducerFactory;
import com.espertech.ESPERQueries.DynatraceAnalysisEsperQueries;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EventGenerators.ComplexEventGenerator;
import com.espertech.EventGenerators.DynatraceEventGenerator;
import com.espertech.EventTypes.Types.dynatrace.DynatraceLog;
import com.espertech.Kafka.config.KafkaEventConfig;
import com.espertech.Main;
import com.espertech.QueriesDatabase.Postgres.PostgresDB;
import com.espertech.QueriesDatabase.QueriesDB;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.espertech.DynatraceRecordsIssueRecordsInserter.seedErrors;
import static com.espertech.Main.esperService;

@Service("kafkaEventServiceV1")
@Scope("singleton")
public class KafkaEventService {
    private final KafkaEventConfig config;

    private MessageBrokerProducer producer;
    private MessageBrokerListener listener;

    private Thread producerThread;
    private Thread consumerThread;

    private QueriesDB queriesDB = new PostgresDB();

    private volatile boolean running = false;

    @Autowired
    public KafkaEventService(KafkaEventConfig config) {
        this.config = config;
    }

    private void initAnalysis() {
        var traceBuffer = TraceBuffer.getInstance();
        var analyzer = new EventAnalyzer(traceBuffer, Main.esperService);

        var properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1048576");
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "104857600");
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "10485760");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");

        properties.put("spring.json.value.default.type", SpanEvent.class.getName());
        properties.put("spring.json.trusted.packages", "*");


        var kafkaAdmin = KafkaAdminClient.create(properties);

        try {
            deleteTopics(kafkaAdmin);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        var topicWatcher = new TopicWatcherService(kafkaAdmin, analyzer::handle);
        topicWatcher.startMonitoring();
    }

    private void deleteTopics(AdminClient kafkaAdmin) throws ExecutionException, InterruptedException {
        var topicNames = kafkaAdmin.listTopics().names().get();

        topicNames.removeIf(t -> t.startsWith("_"));

        if (!topicNames.isEmpty()) {
            System.out.println("Deleting topics: " + topicNames);
            var deleteTopicsResult = kafkaAdmin.deleteTopics(topicNames);
            deleteTopicsResult.all().get();
            System.out.println("All topics deleted.");
        } else {
            System.out.println("No topics to delete.");
        }
    }

    public synchronized void startAnalysis(KafkaEventConfig.ConfigType brokerType) throws IOException {
        stopAnalysis();

        producer = createProducer();
        listener = createListener(brokerType);

        //fetch analysis & analytics queries
        var analyticsQueries = queriesDB.fetchQueries();
        var analysisQueries = queriesDB.fetchAnalysisQueries();

        //deploy queries

        esperService.setListener(listener.getListener());

        try {
            analyticsQueries.forEach(x ->
            {
                if (!x.status) {
                    var res = esperService.deployNewQueryFromDB(x);

                    if (res != null) {
                        queriesDB.updateQueryStatus(res.id, true);
                    }
                }
            });

            esperService.deployAnalysisQueries(analysisQueries);
            analysisQueries.forEach(x -> {
                x.status = true;
                queriesDB.saveUpdatedAnalysisQuery(x); //todo we should use updateQueryStatus to update by uuid
            });
        } catch (Exception e) {
            Main.logger.error("Failed to deploy queries: {}", e.getMessage());
        }

        initAnalysis();

        running = true;

        consumerThread = new Thread(() -> {
            try {
                listener.startListening();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                listener.stopListening();
            }
        });

        switch (brokerType) {
            case KafkaEventConfig.ConfigType.Complex:
                var complexEvents = ComplexEventGenerator.getEvents();
                producerThread = new Thread(() -> {
//                    try {
//                        producer.run(complexEvents);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    } finally {
//                        producer.stop();
//                    }
                });
                break;
            case KafkaEventConfig.ConfigType.Dynatrace:
                //ClassPathResource timeframe1 = new ClassPathResource("traces_spans_astroshop_timeframe1.json");
                //ClassPathResource timeframe2 = new ClassPathResource("traces_spans_astroshop_timeframe2.json");
                //ClassPathResource timeframe1 = new ClassPathResource("testAnalysisEvents2.json");

                var originalRecordsFileName = "traces_spans_astroshop_timeframe1.json"; //"testAnalysisEvents2.json";
                DynatraceEventGenerator eventGen;

                var tempFile = seedErrors(originalRecordsFileName, List.of("issueInvalidSequence.json"), Optional.of(3));

                if (tempFile.isEmpty() || tempFile.equals(originalRecordsFileName)){
                    Main.logger.info("Failed to seed errors, using original file");
                    eventGen = new DynatraceEventGenerator.Builder(new ClassPathResource(originalRecordsFileName).getFile()).setGroupByTraceId(true).build();
                }
                else
                {
                    eventGen = new DynatraceEventGenerator.Builder(new File(tempFile)).setGroupByTraceId(true).build();
                    Main.logger.info("Using seeded file: {}", tempFile);
                    var result = new File(tempFile).delete();
                    Main.logger.info("Temporary file was {}", result ? "deleted" : "NOT deleted");
                }

                var logs = new ArrayList<DynatraceLog>();
                logs.add(eventGen.log);

                //eventGen.readFile(timeframe2.getFile());
                //eventGen.groupByTraceId();
                //logs.add(eventGen.log);

                var allRecords = logs.stream().flatMap(log -> log.records.stream()).toList();

                producerThread = new Thread(() -> {
                    try {
                        producer.run(allRecords);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producer.stop();
                    }
                });
                break;
        }

        consumerThread.start();
        producerThread.start();
    }

    public synchronized void stopAnalysis() {
        if (!running) return;

        running = false;

        if (producer != null) {
            producer.stop();
        }
        if (listener != null) {
            listener.stopListening();
        }

        if (producerThread != null && producerThread.isAlive()) {
            producerThread.interrupt();
            try {
                producerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();
            try {
                consumerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        producerThread = null;
        consumerThread = null;
    }

    private MessageBrokerProducer createProducer() {
        return MessageProducerFactory.createProducer(BrokerType.KAFKA, config.getKafkaTopic(), config.getKafkaBootstrapServers());
    }

    private MessageBrokerListener createListener(KafkaEventConfig.ConfigType type) {
        LSSEsperQueries queries = switch (type) {
            case KafkaEventConfig.ConfigType.Complex -> new ComplexEsperQueries();
            case KafkaEventConfig.ConfigType.Dynatrace -> new DynatraceAnalysisEsperQueries();
            default -> throw new IllegalArgumentException("Unknown listener type: " + type);
        };

        switch (type) {
            case KafkaEventConfig.ConfigType.Complex:
                return MessageListenerFactory.createListener(BrokerType.KAFKA, KafkaEventConfig.ConfigType.Complex, config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            case KafkaEventConfig.ConfigType.Dynatrace:
                return MessageListenerFactory.createListener(BrokerType.KAFKA, KafkaEventConfig.ConfigType.Dynatrace, config.getKafkaTopic(), config.getKafkaBootstrapServers(), queries);
            default:
                throw new IllegalArgumentException("Unknown listener type: " + type);
        }
    }
}
