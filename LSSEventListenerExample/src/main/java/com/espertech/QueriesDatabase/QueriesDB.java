package com.espertech.QueriesDatabase;

import com.espertech.esper.common.internal.collection.Pair;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface QueriesDB {
    Collection<QueryMetadataDTO> fetchQueries();
    CompletableFuture<Void> insertQueriesAsync(Collection<QueryMetadataDTO> queries);
    CompletableFuture<Collection<QueryMetadataDTO>> fetchQueriesAsync();
    Collection<String> getAvailableCategories();
    void insertQueries(Collection<QueryMetadataDTO> queries);
    void insertQuery(QueryMetadataDTO query);
    void updateQueryStatus(UUID queryId, boolean status);
    boolean ping();
    void deleteQuery(UUID queryId);
    void saveUpdatedQuery(QueryMetadataDTO query);
    void resetTable(String tableName);
    void insertAnalysisQueries(Collection<QueryMetadataDTO> queriesWithTypes);
    Collection<QueryMetadataDTO>fetchAnalysisQueries();
}
