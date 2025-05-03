package com.espertech.EventTypes.Types.dynatrace;

import com.espertech.EventTypes.LSSEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynatraceLog {

    @JsonProperty("records")
    public List<DynatraceRecord> records;

    public DynatraceLog() {
        records = new ArrayList<>();
    }
}
