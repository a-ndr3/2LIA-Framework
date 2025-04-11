package com.espertech;

import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;

import java.util.List;

public interface EsperService {
    EPRuntime getRuntime();
    EPDeployment getDeployment();
    Configuration getConfiguration();
    String deployQuery(EsperQueryDTO query);
    QueryMetadataDTO deployNewQuery(String query, String name, List<String> classes, String category, String description);
    String changeExistingQuery(EsperQueryDTO query);
}
