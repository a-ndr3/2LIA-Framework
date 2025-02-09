package com.espertech.ESPERQueries;

import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;
import org.springframework.stereotype.Component;

@Component
public interface LSSEsperQueries {
    void compileEpl(EPRuntime runtime);
    Configuration getConfiguration();
}
