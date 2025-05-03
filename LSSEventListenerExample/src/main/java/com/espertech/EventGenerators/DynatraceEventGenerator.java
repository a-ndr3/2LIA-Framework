package com.espertech.EventGenerators;

import com.espertech.EventTypes.Types.dynatrace.DynatraceLog;
import com.espertech.EventTypes.Types.dynatrace.DynatraceRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DynatraceEventGenerator {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public DynatraceLog log;

    private final boolean groupByTraceId;
    private final boolean groupByServiceId;
    private final boolean groupByServiceName;
    private final boolean groupByTime;

    public void readFile(File file) {
        try {
            log = new DynatraceLog();
            log = objectMapper.readValue(file, DynatraceLog.class);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    public void groupByTraceId(){
        var grouped = log.records.stream()
                .collect(Collectors.groupingBy(DynatraceRecord::getTraceId));

        log.records = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            log.records.addAll(entry.getValue());
        }
    }

    public void groupByServiceId(){
        var grouped = log.records.stream()
                .collect(Collectors.groupingBy(DynatraceRecord::getServiceId));

        log.records = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            log.records.addAll(entry.getValue());
        }
    }

    public void groupByServiceName(){
        var grouped = log.records.stream()
                .collect(Collectors.groupingBy(DynatraceRecord::getServiceName));

        log.records = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            log.records.addAll(entry.getValue());
        }
    }

    public void groupByTime(){
        var grouped = log.records.stream()
                .collect(Collectors.groupingBy(DynatraceRecord::getStartTime));

        log.records = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            log.records.addAll(entry.getValue());
        }
    }

    public static class Builder {
        private final File file;

        public Builder(File file) {
            this.file = file;
        }

        private boolean groupByTraceId = false;
        private boolean groupByServiceId = false;
        private boolean groupByServiceName = false;
        private boolean groupByTime = false;

        public Builder setGroupByTraceId(boolean groupByTraceId) {
            this.groupByTraceId = groupByTraceId;
            return this;
        }

        public Builder setGroupByServiceId(boolean groupByServiceId) {
            this.groupByServiceId = groupByServiceId;
            return this;
        }

        public Builder setGroupByServiceName(boolean groupByServiceName) {
            this.groupByServiceName = groupByServiceName;
            return this;
        }

        public Builder setGroupByTime(boolean groupByTime) {
            this.groupByTime = groupByTime;
            return this;
        }

        public DynatraceEventGenerator build() {
            return new DynatraceEventGenerator(this);
        }
    }

    private DynatraceEventGenerator(Builder builder) {
        objectMapper.registerModule(new JavaTimeModule());
        this.groupByTraceId = builder.groupByTraceId;
        this.groupByServiceId = builder.groupByServiceId;
        this.groupByServiceName = builder.groupByServiceName;
        this.groupByTime = builder.groupByTime;
        this.log = new DynatraceLog();
        readFile(builder.file);
    }
}
