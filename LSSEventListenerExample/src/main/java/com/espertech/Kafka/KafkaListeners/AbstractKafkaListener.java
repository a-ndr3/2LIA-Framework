package com.espertech.Kafka.KafkaListeners;

import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EsperService;
import com.espertech.EsperServiceImpl;
import com.espertech.Main;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.events.ComplexEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.UUID;

public abstract class AbstractKafkaListener {
    protected String topic;
    protected String server;

    protected EPRuntime runtime;
    protected EPDeployment deployment;
    protected Configuration configuration;

    protected KafkaConsumer<String, String> consumer;

    protected EsperService esperService;

    public AbstractKafkaListener(String kafkaTopic, String kafkaBootstrapServers, EsperService esperService, LSSEsperQueries esperQueries, String queriesDeploymentId) {
        this.topic = kafkaTopic;
        this.server = kafkaBootstrapServers;
        this.esperService = esperService;

        try {
            init(esperQueries, queriesDeploymentId);
        } catch (EPCompileException | EPDeployException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(LSSEsperQueries esperQueries, String queriesDeploymentId) throws EPCompileException, EPDeployException, RuntimeException {
        this.consumer = getKafkaConsumer();

        Main.logger.info("Kafka Event Listener initialized");

        configuration = esperQueries.getConfiguration();

        runtime = EPRuntimeProvider.getRuntime("KafkaListener", configuration);
        runtime.initialize();

        esperQueries.compileEpl(runtime, configuration);

        deployment = runtime.getDeploymentService().getDeployment(queriesDeploymentId);

        ((EsperServiceImpl) this.esperService).setRuntime(runtime);
        ((EsperServiceImpl) this.esperService).setDeployment(deployment);
        ((EsperServiceImpl) this.esperService).setConfiguration(configuration);

        Main.logger.info("ESPER deployed");
    }

    private KafkaConsumer<String, String> getKafkaConsumer() throws RuntimeException {
        var properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "80000");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1048576");
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "104857600");
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "10485760");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");

        properties.put("spring.json.value.default.type", ComplexEvent.class.getName());
        properties.put("spring.json.trusted.packages", "*");

        /*
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "40000");
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1048576");
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");
         */

        //properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        //properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        // properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");

        return new KafkaConsumer<>(properties);
    }
}
