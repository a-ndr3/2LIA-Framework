package com.espertech.ESPERQueries;

import java.util.UUID;

public class EsperQueryDTO {
    public String query;
    public String deploymentId;
    public String queryStatement;
    public UUID id;

    public EsperQueryDTO(String query, String deploymentId, String queryStatement, UUID id) {
        this.query = query;
        this.deploymentId = deploymentId;
        this.queryStatement = queryStatement;
        this.id = id;
    }

    public EsperQueryDTO(String query) {
        this.query = query;
        this.deploymentId = "";
        this.queryStatement = "";
        this.id = UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    public EsperQueryDTO(String query, String deploymentId) {
        this.query = query;
        this.deploymentId = deploymentId;
        this.queryStatement = null;
        this.id = null;
    }
}
