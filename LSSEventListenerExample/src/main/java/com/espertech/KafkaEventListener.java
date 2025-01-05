package com.espertech;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaEventListener {
    private final String topic;
    private final String server;
    private EPRuntime runtime;
    private EPDeployment deployment;
    private EPCompiled compiled;
    private Configuration configuration;
    private KafkaConsumer<String,String> consumer;

    public KafkaEventListener(String kafkaTopic, String server) {
        this.topic = kafkaTopic;
        this.server = server;

        try {
            init();
        } catch (EPCompileException | EPDeployException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws EPCompileException, EPDeployException, RuntimeException {
        this.consumer = getKafkaConsumer();

        System.out.println("Kafka Event Listener initialized");

//        this.runtime = EPRuntimeProvider.getDefaultRuntime();
//
//        String epl = "@name('my-statement') select * from MySystemEvent where systemId = 'A4' and type = 'update4';\n";
//
//        compileEPL(epl);
//
//        this.deployment = runtime.getDeploymentService().deploy(compiled);

        configuration = KafkaQueries.getConfiguration();
        compiled = KafkaQueries.compileEpl(configuration);
        runtime = EPRuntimeProvider.getRuntime("KafkaListener", configuration);
        runtime.initialize();
        deployment = KafkaQueries.deploy(runtime, compiled);

        System.out.println("ESPER deployed");
    }

    public void run(){
        consumer.subscribe(Collections.singletonList(topic));

        var statement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "my-statement");

        statement.addListener( (newData, oldData, stat, runtime) -> {
            System.out.println("Event received: " + newData[0].getUnderlying());
        });

        while (true) {
            var records = this.consumer.poll(Duration.ofSeconds(1));
            for (var record : records) {
                System.out.println("Received event: " + record.value());

                var sysId = record.value().split(",")[0].split(":")[1];
                var type = record.value().split(",")[1].split(":")[1];

                runtime.getEventService().sendEventBean(new MySystemEvent(sysId,type), "MySystemEvent");
            }
        }
    }

    private KafkaConsumer<String, String> getKafkaConsumer() throws RuntimeException {
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-events");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");

        return new KafkaConsumer<>(properties);
    }

    private void initConfiguration() {
        this.configuration = new Configuration();
        this.configuration.getCommon().addEventType(MySystemEvent.class); //todo protobuf here?
    }

    private void compileEPL(String epl) throws EPCompileException {
        initConfiguration();
        this.compiled = EPCompilerProvider.getCompiler().compile(epl, new CompilerArguments(this.configuration));
    }
}
