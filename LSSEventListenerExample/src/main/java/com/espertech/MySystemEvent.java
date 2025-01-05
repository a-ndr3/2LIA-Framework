package com.espertech;

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
}
