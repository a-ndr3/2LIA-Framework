package com.espertech.ESPERQueries;

public class LSSQuery {
    public String statement;
    public String deploymentId;

    public LSSQuery(String statement, String deploymentId) {
        this.statement = statement;
        this.deploymentId = deploymentId;
    }
}
