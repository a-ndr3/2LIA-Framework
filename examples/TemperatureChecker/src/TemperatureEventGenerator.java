import java.util.LinkedList;
import java.util.Random;

public class TemperatureEventGenerator {
    private final Random random = new Random(System.currentTimeMillis());

    private final String[] locations = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    public LinkedList<Object> makeEventStream(int numberOfTicks) {
        var stream = new LinkedList<>();

        for (int i = 0; i < numberOfTicks; i++) {
            var tick = new Temperature(
                    locations[random.nextInt(locations.length)],
                    random.nextDouble(-50, 50));

            stream.add(tick);
        }

        return stream;
    }
}
