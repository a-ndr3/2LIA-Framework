package com.espertech;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.EventListeners.ComplexEventListener;
import com.espertech.Kafka.KafkaListeners.KafkaComplexEventListener;
import com.espertech.Kafka.KafkaListeners.KafkaSimpleEventListener;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventProducer;
import com.espertech.Kafka.KafkaProducers.KafkaSimpleEventProducer;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.Kafka.config.KafkaEventConfig;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.events.ComplexEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.common.ConfigurationCommonEventTypeObjectArray;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esperio.kafka.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Properties;
import java.util.UUID;

//@SpringBootApplication
public class Main {
 //   public static final Logger logger = LoggerFactory.getLogger(Main.class);
  //  static EsperService esperService; //todo inject into controller
   // static KafkaEventConfig config = new KafkaEventConfig(KafkaEventConfig.ConfigType.Complex);

    public static void main(String[] args) {
        LSSKafkaProducer producer;
        try {
            var props = new Properties();

            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "my_group_id" + UUID.randomUUID());

            props.put(EsperIOKafkaConfig.INPUT_SUBSCRIBER_CONFIG, EsperIOKafkaInputSubscriberByTopicList.class.getName());
            props.put(EsperIOKafkaConfig.TOPICS_CONFIG, "complexEvents");
            props.put(EsperIOKafkaConfig.INPUT_PROCESSOR_CONFIG, EsperIOKafkaInputProcessorDefault.class.getName());
            props.put(EsperIOKafkaConfig.INPUT_TIMESTAMPEXTRACTOR_CONFIG, EsperIOKafkaInputTimestampExtractorConsumerRecord.class.getName());

            props.put("spring.json.value.default.type", ComplexEvent.class.getName());
            props.put("spring.json.trusted.packages", "*");

            Configuration configuration = new Configuration();
            configuration.getCommon().addEventType("ComplexEvent", ComplexEvent.class);
            EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);

            Configuration cfg = new Configuration();
            cfg.getCommon().addEventType(ComplexEvent.class);
            String simpleSelect = "@name('my-statement') select * from ComplexEvent where value < -40 or value > 60;";
            EPCompiled selectCompiled = EPCompilerProvider.getCompiler().compile(simpleSelect, new CompilerArguments(cfg));
            runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId("complexSelectQueries"));

            var adapter = new EsperIOKafkaInputAdapter(props, runtime.getURI());
            adapter.start();

            producer = new KafkaComplexEventProducer("complexEvents", "localhost:9092");

            var listener = new ComplexEventListener();

            runtime.getDeploymentService()
                    .getStatement("complexSelectQueries", "my-statement")
                    .addListener(listener);

            Thread producerThread = new Thread(() -> producer.run(1));
            producerThread.start();

            Thread.currentThread().join();

//        try {
//            logger.info("Application started");
//            esperService = new EsperServiceImpl();
//            SpringApplication.run(Main.class, args);
//        } catch (Exception e) {
//            logger.error("An error occurred: {}", e.getMessage(), e);
//        }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    //http://localhost:8081/swagger-ui/index.html

//    @Bean
//    public LSSKafkaProducer kafkaEventProducer() {
//        return new KafkaComplexEventProducer(config.kafkaTopic(), config.kafkaBootstrapServers());
//    }
//
//    @Bean
//    public LSSKafkaListener kafkaEventListener() {
//        LSSEsperQueries queries = new ComplexEsperQueries();
//        return new KafkaComplexEventListener(config.kafkaTopic(), config.kafkaBootstrapServers(), esperService, queries);
//    }
}
















/*
package com.espertech;

import com.espertech.ESPERQueries.ComplexEsperQueries;
import com.espertech.ESPERQueries.LSSEsperQueries;
import com.espertech.Kafka.KafkaListeners.KafkaComplexEventListener;
import com.espertech.Kafka.KafkaListeners.KafkaSimpleEventListener;
import com.espertech.Kafka.KafkaProducers.KafkaComplexEventProducer;
import com.espertech.Kafka.KafkaProducers.KafkaSimpleEventProducer;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.Kafka.config.KafkaEventConfig;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.events.ComplexEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.configuration.common.ConfigurationCommonEventTypeObjectArray;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import com.espertech.esperio.kafka.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.*;

//@SpringBootApplication
public class Main {
 //   public static final Logger logger = LoggerFactory.getLogger(Main.class);
  //  static EsperService esperService; //todo inject into controller
   // static KafkaEventConfig config = new KafkaEventConfig(KafkaEventConfig.ConfigType.Complex);
 public static Properties getInputPluginProps(String topicName, String valueDeserializerClassName, String timestampExtractorClassName) {
     Properties props = new Properties();
     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClassName);
     props.put(ConsumerConfig.GROUP_ID_CONFIG, topicName + "__mygroup");
     props.put(EsperIOKafkaConfig.INPUT_SUBSCRIBER_CONFIG, EsperIOKafkaInputSubscriberByTopicList.class.getName());
     props.put(EsperIOKafkaConfig.TOPICS_CONFIG, topicName);
     props.put(EsperIOKafkaConfig.INPUT_PROCESSOR_CONFIG, EsperIOKafkaInputProcessorDefault.class.getName());
     if (timestampExtractorClassName != null) {
         props.put(EsperIOKafkaConfig.INPUT_TIMESTAMPEXTRACTOR_CONFIG, timestampExtractorClassName);
     }
     return props;
 }
    public static KafkaConsumer<String, Object> initConsumer(String topicName,String valueDeserializerClassName) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClassName);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, topicName + "__mygroup");
        return new KafkaConsumer<>(consumerProps);
    }
    public static void main(String[] args) {
        LSSKafkaProducer producer;
        try {
            var props = new Properties();

//            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
//            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
//            props.put(ConsumerConfig.GROUP_ID_CONFIG, "my_group_id");
//
//            props.put(EsperIOKafkaConfig.INPUT_SUBSCRIBER_CONFIG, EsperIOKafkaInputSubscriberByTopicList.class.getName());
//            props.put(EsperIOKafkaConfig.TOPICS_CONFIG, "complexEvents");
//            props.put(EsperIOKafkaConfig.INPUT_PROCESSOR_CONFIG, EsperIOKafkaInputProcessorDefault.class.getName());
//            props.put(EsperIOKafkaConfig.INPUT_TIMESTAMPEXTRACTOR_CONFIG, EsperIOKafkaInputTimestampExtractorConsumerRecord.class.getName());
//

            Properties pluginProperties = getInputPluginProps("complexEvents", JsonDeserializer.class.getName(), null);
            pluginProperties.setProperty(EsperIOKafkaConfig.INPUT_PROCESSOR_CONFIG, EsperIOKafkaInputProcessorJson.class.getName());
            pluginProperties.setProperty(EsperIOKafkaConfig.INPUT_EVENTTYPENAME, "ComplexEvent");

            Configuration configuration = new Configuration();
            configuration.getRuntime().addPluginLoader(EsperIOKafkaInputAdapterPlugin.class.getSimpleName(), EsperIOKafkaInputAdapterPlugin.class.getName(), pluginProperties, "esper-kafka-config.xml");
            configuration.getCommon().addEventType("ComplexEvent", ComplexEvent.class);
            EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(configuration);

            String simpleSelect = "@name('my-statement') select * from ComplexEvent where value < -40 or value > 60;";
            EPCompiled selectCompiled = EPCompilerProvider.getCompiler().compile(simpleSelect, new CompilerArguments(configuration));
            runtime.getDeploymentService().deploy(selectCompiled, new DeploymentOptions().setDeploymentId("complexSelectQueries"));

//            KafkaConsumer<String, Object> consumer = initConsumer("complexEvents", JsonDeserializer.class.getName());
//            Collection<TopicPartition> topicPartitions = Collections.singletonList(new TopicPartition("complexEvent", 0));
//            consumer.assign(topicPartitions);
//            consumer.seek(topicPartitions.iterator().next(), 0);

            producer = new KafkaComplexEventProducer("complexEvents", "localhost:9092");

            runtime.getDeploymentService()
                    .getStatement("complexSelectQueries", "my-statement")
                    .addListener((newEvents, oldEvents, stmt, rt) -> {
                        if (newEvents != null) {
                            for (var event : newEvents) {
                                System.out.println("Processed event: " + event.getUnderlying());
                            }
                        }
                    });

            Thread producerThread = new Thread(() -> producer.run(1));
            producerThread.start();

            Thread.currentThread().join();

//        try {
//            logger.info("Application started");
//            esperService = new EsperServiceImpl();
//            SpringApplication.run(Main.class, args);
//        } catch (Exception e) {
//            logger.error("An error occurred: {}", e.getMessage(), e);
//        }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    //http://localhost:8081/swagger-ui/index.html

//    @Bean
//    public LSSKafkaProducer kafkaEventProducer() {
//        return new KafkaComplexEventProducer(config.kafkaTopic(), config.kafkaBootstrapServers());
//    }
//
//    @Bean
//    public LSSKafkaListener kafkaEventListener() {
//        LSSEsperQueries queries = new ComplexEsperQueries();
//        return new KafkaComplexEventListener(config.kafkaTopic(), config.kafkaBootstrapServers(), esperService, queries);
//    }
}
 */