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

//    public static ComplexEvent fromByteBuffer(ByteBuffer byteBuffer) {
//        ComplexEvent event = new ComplexEvent();
//
//        event.eventId = byteBuffer.getInt();
//
//        char[] eventNameChars = new char[20]; // assumed length 20
//        for (int i = 0; i < eventNameChars.length; i++) {
//            eventNameChars[i] = byteBuffer.getChar();
//        }
//        event.eventName = new String(eventNameChars).trim();
//
//        char[] eventTypeChars = new char[10]; // assumed length 10
//        for (int i = 0; i < eventTypeChars.length; i++) {
//            eventTypeChars[i] = byteBuffer.getChar();
//        }
//
//        event.eventType = new String(eventTypeChars).trim();
//        event.timestamp = byteBuffer.getLong();
//        event.value = byteBuffer.getDouble();
//        event.percentage = byteBuffer.getFloat();
//        event.isActive = byteBuffer.get() == 1;
//        event.level = byteBuffer.getShort();
//        event.category = byteBuffer.getChar();
//        event.priority = byteBuffer.get();
//
//        char[] sourceChars = new char[15]; // assumed length 15
//        for (int i = 0; i < sourceChars.length; i++) {
//            sourceChars[i] = byteBuffer.getChar();
//        }
//
//        event.source = new String(sourceChars).trim();
//
//        char[] destinationChars = new char[15]; // assumed length 15
//        for (int i = 0; i < destinationChars.length; i++) {
//            destinationChars[i] = byteBuffer.getChar();
//        }
//
//        event.destination = new String(destinationChars).trim();
//
//        event.duration = byteBuffer.getLong();
//
//        int metadataSize = byteBuffer.getInt();
//        Map<String, String> metadata = new HashMap<>();
//        for (int i = 0; i < metadataSize; i++) {
//            char[] keyChars = new char[10];
//            for (int j = 0; j < keyChars.length; j++) {
//                keyChars[j] = byteBuffer.getChar();
//            }
//            String key = new String(keyChars).trim();
//
//            char[] valueChars = new char[20];
//            for (int j = 0; j < valueChars.length; j++) {
//                valueChars[j] = byteBuffer.getChar();
//            }
//            String value = new String(valueChars).trim();
//
//            metadata.put(key, value);
//        }
//
//        event.metadata = metadata;
//
//        int dataPointsSize = byteBuffer.getInt();
//        List<Integer> dataPoints = new ArrayList<>();
//        for (int i = 0; i < dataPointsSize; i++) {
//            dataPoints.add(byteBuffer.getInt());
//        }
//
//        event.dataPoints = dataPoints;
//
//        return event;
//    }
}

