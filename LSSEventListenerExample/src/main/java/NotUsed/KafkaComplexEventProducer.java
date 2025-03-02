package NotUsed;

import com.espertech.Kafka.LSSKafkaProducer;
import com.espertech.events.ComplexEvent;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;

public class KafkaComplexEventProducer extends AbstractKafkaProducer implements LSSKafkaProducer {

    public KafkaComplexEventProducer(String kafkaTopic, String kafkaBootstrapServers) {
        super(kafkaTopic, kafkaBootstrapServers);
    }

    static List<String> eventType = List.of("Event1", "Event2", "Event3", "Event4", "Event5");
    static List<String> source = List.of("Source1", "Source2", "Source3", "Source4", "Source5");
    static List<Integer> dataPoints = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
    static Map<String, String> meta = Map.of(
            "meta1", "meta1_v",
            "meta2", "meta2_v",
            "meta3", "meta3_v",
            "meta4", "meta4_v",
            "meta5", "meta5_v"
    );
    static char[] category = new char[]{'A', 'B', 'C', 'D', 'E'};
    static List<String> destination = List.of("Dest1", "Dest2", "Dest3", "Dest4", "Dest5");
    static byte[] priority = new byte[]{1, 2, 3, 4, 5};
    static long[] duration = new long[]{1000, 2000, 3000, 4000, 5000};

    public void run() {
        int i = 0;
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            try {
                Thread.sleep(1 * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            var mmap = new HashMap<String, String>();
            var rnd = random.nextInt(0, meta.size());
            var rnd2 = random.nextInt(0, meta.size());
            mmap.put("meta1" + rnd, meta.get("meta" + rnd));
            mmap.put("meta2" + rnd2, meta.get("meta" + rnd2));

            var dataP = new ArrayList<Integer>();
            dataP.add(dataPoints.get(i % dataPoints.size()));
            dataP.add(dataPoints.get((i + 1) % dataPoints.size()));

            var obj = new ComplexEvent(
                    i,
                    "EventName" + i,
                    eventType.get(i % eventType.size()),
                    System.currentTimeMillis(),
                    random.nextDouble(-100.0, 100.0),
                    random.nextFloat(0.0f, 100.0f),
                    random.nextBoolean(),
                    ((short) random.nextInt(0, 10000)),
                    category[i % category.length],
                    priority[i % priority.length],
                    source.get(i % source.size()),
                    destination.get(i % destination.size()),
                    duration[i % duration.length],
                    mmap,
                    dataP
            );

            var record = new ProducerRecord<String, Object>(topic, obj);
            producer.send(record);

            i++;
        }
    }

    @Override
    public void stop() {
        running = false;
    }
}
