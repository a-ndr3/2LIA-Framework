package NotUsed;

public class MySystemEvent {
    private String systemId;
    private String type;

    public MySystemEvent(String systemId, String type) {
        this.systemId = systemId;
        this.type = type;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MySystemEvent{" +
                "systemId='" + systemId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
