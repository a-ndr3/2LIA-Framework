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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

@Component
public class KafkaEventListener {
    private final String topic;
    private final String server;
    private EPRuntime runtime;
    private EPDeployment deployment;
    private EPCompiled compiled;
    private Configuration configuration;
    private KafkaConsumer<String, String> consumer;

    private EsperService esperService;

    @Autowired
    public KafkaEventListener(String kafkaTopic, String kafkaBootstrapServers, EsperService esperService) {
        this.topic = kafkaTopic;
        this.server = kafkaBootstrapServers;
        this.esperService = esperService;

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
        runtime = EPRuntimeProvider.getRuntime("KafkaListener", configuration);
        runtime.initialize();
        KafkaQueries.compileEpl(runtime, configuration);
        deployment = runtime.getDeploymentService().getDeployment("myEventQueries3");

        ((EsperServiceImpl) this.esperService).setRuntime(runtime);
        ((EsperServiceImpl) this.esperService).setDeployment(deployment);
        //((EsperServiceImpl) this.esperService).setCompiled(compiled);
        ((EsperServiceImpl) this.esperService).setConfiguration(configuration);

        System.out.println("ESPER deployed");
    }

    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

            var statement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "my-statement");

            statement.addListener((newData, oldData, stat, runtime) -> {
                System.out.println("Event received: " + ((MySystemEvent) (newData[0].getUnderlying())).toString()); //(Temperature) event.getUnderlying();
            });

            while (true) {
                var records = this.consumer.poll(Duration.ofMillis(100));
                for (var record : records) {
                    //System.out.println("Received event: " + record.value());

                    var sysId = record.value().split(",")[0].split(":")[1];
                    var type = record.value().split(",")[1].split(":")[1];

                    runtime.getEventService().sendEventBean(new MySystemEvent(sysId, type), "MySystemEvent");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
        }
    }

    public EsperService getEsperService() {
        return esperService;
    }

    private KafkaConsumer<String, String> getKafkaConsumer() throws RuntimeException {
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
       // properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");

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
