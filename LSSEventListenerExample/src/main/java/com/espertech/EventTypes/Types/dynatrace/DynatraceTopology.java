package com.espertech.EventTypes.Types.dynatrace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynatraceTopology {
    @JsonProperty("records")
    private List<DynatraceTopologyRecord> records;

    public List<DynatraceTopologyRecord> getRecords() {
        return records;
    }

    private static DynatraceTopology fromFile(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(file, DynatraceTopology.class);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    public static DynatraceTopology getTopology() {
        try {
            ClassPathResource resource = new ClassPathResource("services_topology.json");
            return fromFile(resource.getFile());
        } catch (Exception e) {
            throw new RuntimeException("Error reading classpath resource services_topology.json: ", e);
        }
    }
}
