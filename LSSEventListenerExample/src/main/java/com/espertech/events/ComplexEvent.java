package com.espertech.events;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexEvent {
    public int eventId;
    public String eventName;
    public String eventType;
    public long timestamp;
    public double value;
    public float percentage;
    public boolean isActive;
    public short level;
    public char category;
    public byte priority;

    public List<Integer> getDataPoints() {
        return dataPoints;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public long getDuration() {
        return duration;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public byte getPriority() {
        return priority;
    }

    public char getCategory() {
        return category;
    }

    public short getLevel() {
        return level;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getPercentage() {
        return percentage;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public int getEventId() {
        return eventId;
    }

    public String source;
    public String destination;
    public long duration;
    public Map<String, String> metadata;
    public List<Integer> dataPoints;

    public ComplexEvent() {
    }

    public ComplexEvent(int eventId, String eventName, String eventType, long timestamp, double value, float percentage,
                        boolean isActive, short level, char category, byte priority, String source, String destination,
                        long duration, Map<String, String> metadata, List<Integer> dataPoints) {

        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.value = value;
        this.percentage = percentage;
        this.isActive = isActive;
        this.level = level;
        this.category = category;
        this.priority = priority;
        this.source = source;
        this.destination = destination;
        this.duration = duration;
        this.metadata = metadata;
        this.dataPoints = dataPoints;
    }

    @Override
    public String toString() {
        return "ComplexEvent{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                ", percentage=" + percentage +
                ", isActive=" + isActive +
                ", level=" + level +
                ", category=" + category +
                ", priority=" + priority +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", duration=" + duration +
                ", metadata=" + metadata +
                ", dataPoints=" + dataPoints +
                '}';
    }
}

