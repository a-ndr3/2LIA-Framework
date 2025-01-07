package com.espertech;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EsperServiceImpl implements EsperService {
    private EPRuntime runtime;
    private EPDeployment deployment;
    private EPCompiled compiled;
    private Configuration configuration;

    public EsperServiceImpl() {

    }

    public void setRuntime(EPRuntime runtime) {
        this.runtime = runtime;
    }

    public void setDeployment(EPDeployment deployment) {
        this.deployment = deployment;
    }

    public void setCompiled(EPCompiled compiled) {
        this.compiled = compiled;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public EPRuntime getRuntime() {
        return runtime;
    }

    @Override
    public EPDeployment getDeployment() {
        return deployment;
    }

    @Override
    public EPCompiled getCompiled() {
        return compiled;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
