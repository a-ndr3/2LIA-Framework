package com.espertech;

import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.internal.collection.Pair;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.UpdateListener;

import java.util.Collection;
import java.util.List;

public interface EsperService {
    EPRuntime getRuntime();
    EPDeploymentService getDeployment();
    Configuration getConfiguration();
    String deployQuery(EsperQueryDTO query);
    QueryMetadataDTO deployNewQuery(String query, String name, List<String> classes, String category, String description);
    QueryMetadataDTO deployNewQueryFromDB(QueryMetadataDTO queryMetadata);
    String changeExistingQuery(EsperQueryDTO query);
    String changeExistingAnalysisQuery(EsperQueryDTO query);
    UpdateListener getListener();
    void setListener(UpdateListener listener);
    QueryMetadataDTO deployNewQueryNoDefaultListener(String query, String name, List<String> classes, String category, String description);
    void deployAnalysisQueries(Collection<QueryMetadataDTO> topicEplStatements);
    boolean undeployQuery(EsperQueryDTO queryMetadata);
}
