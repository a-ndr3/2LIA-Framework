public class TemperatureAlert {
    private final Temperature temperature;
    private final String location;

    public TemperatureAlert(Temperature temperature, String location) {
        this.temperature = temperature;
        this.location = location;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public String toString() {
        return "TemperatureAlert{" +
            "temperature=" + temperature +
            ", location=" + location +
            '}';
    }
}
