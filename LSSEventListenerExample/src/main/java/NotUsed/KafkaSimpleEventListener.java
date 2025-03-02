package NotUsed;

import com.espertech.EsperService;
import com.espertech.Kafka.KafkaListeners.AbstractKafkaListener;
import com.espertech.Kafka.LSSKafkaListener;
import com.espertech.ESPERQueries.LSSEsperQueries;

import java.time.Duration;
import java.util.Collections;

public class KafkaSimpleEventListener extends AbstractKafkaListener implements LSSKafkaListener {

    public KafkaSimpleEventListener(String kafkaTopic,
                                    String kafkaBootstrapServers,
                                    EsperService esperService,
                                    LSSEsperQueries esperQueries) {
        super(kafkaTopic, kafkaBootstrapServers, esperService, esperQueries, SimpleEsperQueries.staticQueriesDeploymentId);
    }

    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

//---------- DEBUG
//           var statement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "my-statement");
//            statement.addListener((newData, oldData, stat, runtime) -> {
//                Main.logger.debug("Event received: {}", ((MySystemEvent) (newData[0].getUnderlying())).toString());
//            });
//---------- DEBUG

            while (running) {
                var records = this.consumer.poll(Duration.ofMillis(100));
                for (var record : records) {

                    //var sysId = record.value().split(",")[0].split(":")[1];
                    //var type = record.value().split(",")[1].split(":")[1];

                    //runtime.getEventService().sendEventBean(new MySystemEvent(sysId, type), "MySystemEvent");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
        }
    }

    @Override
    public void stop() {
        running = false;
    }
}
