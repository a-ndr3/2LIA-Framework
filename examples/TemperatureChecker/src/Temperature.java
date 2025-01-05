public class Temperature {
    private final String location;
    private final double temperature;

    public Temperature(String location, double temperature) {
        this.location = location;
        this.temperature = temperature;
    }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return "location=" + location + "  temperature=" + temperature;
    }
}
