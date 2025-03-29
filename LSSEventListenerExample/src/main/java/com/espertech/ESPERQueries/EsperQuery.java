package com.espertech.ESPERQueries;

import java.util.UUID;

public class EsperQuery {
    public String query;
    public String deploymentId;
    public String queryStatement;
    public UUID id;

    public EsperQuery(String query, String deploymentId, String queryStatement, UUID id) {
        this.query = query;
        this.deploymentId = deploymentId;
        this.queryStatement = queryStatement;
        this.id = id;
    }
}
