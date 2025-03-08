package com.espertech.QueriesDatabase;

import java.util.List;
import java.util.UUID;

public class QueryUploader {
    private final QueriesDB database;

    public QueryUploader(QueriesDB db) {
        database = db;
    }

    public void uploadQuery(String name, String query, List<String> eventClass, String category, String description) {
        QueryMetadata queryMetadata = new QueryMetadata(
                UUID.randomUUID(),
                name,
                "",
                name+"DeploymentId",
                query,
                eventClass,
                category,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                true,
                description
        );

        database.insertQuery(queryMetadata);
    }

    public void uploadQuery(QueryMetadata queryMetadata) {
        database.insertQuery(queryMetadata);
    }

    public void uploadQueries(List<QueryMetadata> queries){
        database.insertQueries(queries);
    }
}
