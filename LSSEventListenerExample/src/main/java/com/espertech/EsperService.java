package com.espertech;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;

public interface EsperService {
    EPRuntime getRuntime();

    EPDeployment getDeployment();

    EPCompiled getCompiled();

    Configuration getConfiguration();
}
