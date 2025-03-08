package com.espertech.QueriesDatabase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record QueryMetadata(UUID id, String name, String queryStatement, String deploymentId,
                            String query, List<String> eventClasses, String category, Long createdAt,
                            Long updatedAt, Boolean status, String description) {
    public QueryMetadata{
        Objects.requireNonNull(name);
        Objects.requireNonNull(deploymentId);
        Objects.requireNonNull(query);
        Objects.requireNonNull(eventClasses);
        Objects.requireNonNull(category);
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
