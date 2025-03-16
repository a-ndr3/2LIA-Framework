package com.espertech.QueriesDatabase;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface QueriesDB {
    Collection<QueryMetadata> fetchQueries();
    CompletableFuture<Void> insertQueriesAsync(Collection<QueryMetadata> queries);
    CompletableFuture<Collection<QueryMetadata>> fetchQueriesAsync();
    Collection<String> getAvailableCategories();
    void insertQueries(Collection<QueryMetadata> queries);
    void insertQuery(QueryMetadata query);
    void updateQueryStatus(UUID queryId, boolean status);
    boolean ping();
}
