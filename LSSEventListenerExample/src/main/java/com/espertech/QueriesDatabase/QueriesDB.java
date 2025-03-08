package com.espertech.QueriesDatabase;

import java.util.Collection;
import java.util.UUID;

public interface QueriesDB {
    Collection<QueryMetadata> fetchQueries();
    Collection<String> getAvailableCategories();
    void insertQueries(Collection<QueryMetadata> queries);
    void insertQuery(QueryMetadata query);
    void updateQueryStatus(UUID queryId, boolean status);
    boolean ping();
}
