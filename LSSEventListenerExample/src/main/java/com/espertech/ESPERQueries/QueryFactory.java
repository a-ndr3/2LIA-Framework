package com.espertech.ESPERQueries;

import java.util.concurrent.atomic.AtomicInteger;

public class QueryFactory {
    public static QueryFactory instance;
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private static IQueryID queryIDImpl;

    public static QueryFactory getInstance() {
        if (instance == null) {
            instance = new QueryFactory();
            queryIDImpl = IDCreator.getInstance();
        }
        return instance;
    }

    public EsperQueryDTO createQuery(String query){
        var deploymentId = "deploymentId" + idCounter.incrementAndGet();
        return createQuery(query, deploymentId);
    }

    public EsperQueryDTO createQuery(String query, String deploymentId){
        var queryStatement = "queryStatement" + idCounter.incrementAndGet();
        return createQuery(query, deploymentId, queryStatement);
    }

    public EsperQueryDTO createQuery(String query, String deploymentId, String queryStatement){
        var id = queryIDImpl.getQueryID();
        return new EsperQueryDTO(query, deploymentId, queryStatement, id);
    }
}
