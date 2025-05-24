package com.espertech.QueriesDatabase;

import java.util.List;
import java.util.UUID;

public class QueryMetadataDTO {
    public UUID id;
    public String name;
    public String queryStatement;
    public String deploymentId;
    public String query;
    public List<String> eventClasses;
    public String category;
    public Long createdAt;
    public Long updatedAt;
    public Boolean status;
    public String description;

    public QueryMetadataDTO(String id, String name, String queryStatement, String deploymentId,
                            String query, List<String> eventClasses, String category, Long createdAt,
                            Long updatedAt, Boolean status, String description){

        this.id = UUID.fromString(id);
        this.name = name;
        this.queryStatement = queryStatement;
        this.deploymentId = deploymentId;
        this.query = query;
        this.eventClasses = eventClasses;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.description = description;
    }

    public QueryMetadataDTO() {

    }

    public QueryMetadataDTO(String query, String category, Boolean status, Boolean checkTopology){
        this.query = query;
        this.category = category;
        this.status = status;
        this.id = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.name = "";
        this.queryStatement = "";
        this.deploymentId = "";
        this.eventClasses = List.of("");
        this.createdAt = 0L;
        this.updatedAt = 0L;
        this.description = checkTopology ? "1" : "";
    }

    public QueryMetadataDTO(UUID id, String name, String queryStatement, String deploymentId,
                            String query, List<String> eventClasses, String category, Long createdAt,
                            Long updatedAt, Boolean status, String description) {
        this(id.toString(), name, queryStatement, deploymentId, query, eventClasses, category, createdAt, updatedAt, status, description);
    }

    @Override
    public String toString() {
        return "QueryMetadata{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", queryStatement='" + queryStatement + '\'' +
                ", deploymentId='" + deploymentId + '\'' +
                ", query='" + query + '\'' +
                ", eventClasses=" + eventClasses.stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("No event classes") +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status=" + status +
                ", description='" + description + '\'' +
                '}';
    }
}
