package com.espertech.Kafka.KafkaListeners;

import com.espertech.AnalysisCore.Types.SpanEvent;
import com.espertech.EventTypes.Types.dynatrace.DynatraceLog;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Properties;
import java.util.UUID;

import static com.espertech.Main.config;

public class KafkaConsumerFactory {
    public static KafkaConsumer<String, DynatraceRecord> create() {
        var properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "analysis-service-group");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1048576");
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "104857600");
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "10485760");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        properties.put("spring.json.value.default.type", DynatraceRecord.class.getName());
        properties.put("spring.json.trusted.packages", "*");

        return new KafkaConsumer<>(properties);
    }

    public static KafkaConsumer<String, DynatraceRecord> create(String groupId) {
        var properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1048576");
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "104857600");
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "10485760");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        properties.put("spring.json.value.default.type", DynatraceRecord.class.getName());
        properties.put("spring.json.trusted.packages", "*");

        return new KafkaConsumer<>(properties);
    }
}
