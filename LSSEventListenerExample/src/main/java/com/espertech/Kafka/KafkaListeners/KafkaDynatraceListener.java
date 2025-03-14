package com.espertech.Kafka.KafkaListeners;

import com.espertech.Brokers.Listeners.AbstractMessageListener;
import com.espertech.Brokers.Producers.MessageProducerFactory;
import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.DynatraceEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.EventTypes.Types.DynatraceEvent;
import com.espertech.Kafka.ESPERAnalysisOutputUpdateListener;
import com.espertech.Main;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class KafkaDynatraceListener extends AbstractMessageListener {
    private final String server;
    private KafkaConsumer<String, String> consumer;

    ESPERAnalysisOutputUpdateListener listener = new ESPERAnalysisOutputUpdateListener(
            MessageProducerFactory.createProducer("kafka", Main.config.kafkaTopic, Main.config.kafkaBootstrapServers));

    public KafkaDynatraceListener(String kafkaTopic, String kafkaBootstrapServers, EsperService esperService, LSSEsperQueries esperQueries, String initDeploymentId) {
        super(kafkaTopic, esperService, esperQueries, initDeploymentId);
        this.server = kafkaBootstrapServers;
        this.consumer = createKafkaConsumer();
        Main.logger.info("Kafka Listener initialized for topic: {}", kafkaTopic);
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        var properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
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

        properties.put("spring.json.value.default.type", DynatraceEvent.class.getName());
        properties.put("spring.json.trusted.packages", "*");

        return new KafkaConsumer<>(properties);
    }

    @Override
    public void startListening() {
        runtime.getDeploymentService().getStatement(DynatraceEsperQueries.staticQueriesDeploymentId, "my-statement").addListener(listener);

        consumer.subscribe(Collections.singletonList(topic));

        Main.logger.info("Kafka listener started for topic: {}", topic);

        while (running) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                continue;
            }
            processRecords(records);
        }
    }

    private void processRecords(ConsumerRecords<String, String> records) {
        for (var record : records) {
            runtime.getEventService().sendEventBean(record.value(), "DynatraceEvent");
        }
        Main.logger.info("Processed {} records", records.count());
    }

    @Override
    public void stopListening() {
        running = false;
        consumer.close();
        Main.logger.info("Kafka listener stopped for topic: " + topic);
    }
}
