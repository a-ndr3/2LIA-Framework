package com.espertech;

import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.ESPERQueries.QueryFactory;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EsperServiceImpl implements EsperService {
    private static EsperServiceImpl instance = null;

    private EPRuntime runtime;
    private EPDeployment deployment;
    private Configuration configuration;

    private UpdateListener listener; //TODO FIX we need to pass listeners based on the events we listen to -> this one is quick fix for now

    private EsperServiceImpl() {

    }

    public static EsperServiceImpl getInstance() {
        if (instance == null) {
            instance = new EsperServiceImpl();
        }
        return instance;
    }

    public UpdateListener getListener() {
        return listener;
    }

    public void setListener(UpdateListener listener) { //TODO FIX we need to pass listeners based on the events we listen to -> this one is quick fix for now
        this.listener = listener;
    }

    @Override
    public QueryMetadataDTO deployNewQueryNoDefaultListener(String epl, String name, List<String> classes, String category, String description) {
        EPCompiled compiled;
        try {
            CompilerArguments compilerArguments = new CompilerArguments(configuration);
            compilerArguments.getPath().add(runtime.getRuntimePath());
            compiled = EPCompilerProvider.getCompiler().compile(epl, compilerArguments);
        } catch (EPCompileException e) {
            return null;
        }

        EsperQueryDTO newQuery;
        try {
            newQuery = QueryFactory.getInstance().createQuery(epl);
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(newQuery.deploymentId));
        } catch (EPDeployException | IllegalStateException e) {
            return null;
        }

        QueryMetadataDTO data;

        try {
            data = new QueryMetadataDTO(
                    newQuery.id,
                    name,
                    newQuery.queryStatement,
                    newQuery.deploymentId,
                    newQuery.query,
                    classes,
                    category,
                    Instant.now().toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    true,
                    description
            );

        } catch (Exception e) {
            return null;
        }

        return data;
    }

    public void setRuntime(EPRuntime runtime) {
        this.runtime = runtime;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public EPRuntime getRuntime() {
        return runtime;
    }

    @Override
    public EPDeploymentService getDeployment() {
        return Main.esperService.getRuntime().getDeploymentService();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String deployQuery(EsperQueryDTO query) {
        EPCompiled compiled;
        try {
            CompilerArguments compilerArguments = new CompilerArguments(configuration);
            compilerArguments.getPath().add(runtime.getRuntimePath());
            compiled = EPCompilerProvider.getCompiler().compile(query.query, compilerArguments);
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(query.deploymentId));
            runtime.getDeploymentService().getStatement(query.deploymentId, query.queryStatement).addListener(listener); //TODO FIX we need to pass listeners based on the events we listen to -> this one is quick fix for now
        } catch (EPCompileException | EPDeployException | IllegalStateException e) {
            return e.getMessage();
        }
        return "";
    }

    @Override
    public QueryMetadataDTO deployNewQuery(String epl, String name, List<String> classes, String category, String description) {
        EPCompiled compiled;
        try {
            CompilerArguments compilerArguments = new CompilerArguments(configuration);
            compilerArguments.getPath().add(runtime.getRuntimePath());
            compiled = EPCompilerProvider.getCompiler().compile(epl, compilerArguments);
        } catch (EPCompileException e) {
            return null;
        }

        EsperQueryDTO newQuery;
        try {
            newQuery = QueryFactory.getInstance().createQuery(epl);
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(newQuery.deploymentId));
            runtime.getDeploymentService().getStatement(newQuery.deploymentId, newQuery.queryStatement).addListener(listener); //TODO FIX we need to pass listeners based on the events we listen to -> this one is quick fix for now
        } catch (EPDeployException | IllegalStateException e) {
            return null;
        }

        QueryMetadataDTO data;

        try {
            data = new QueryMetadataDTO(
                    newQuery.id,
                    name,
                    name + newQuery.queryStatement,
                    name + newQuery.deploymentId,
                    newQuery.query,
                    classes,
                    category,
                    Instant.now().toEpochMilli(),
                    Instant.now().toEpochMilli(),
                    true,
                    description
            );

        } catch (Exception e) {
            return null;
        }

        return data;
    }

    @Override
    public String changeExistingQuery(EsperQueryDTO query) {
        EPCompiled compiled;
        ArrayList<UpdateListener> existingListeners = new ArrayList<>();
        try {
            try {
                runtime.getDeploymentService().getStatement(query.deploymentId, query.queryStatement).getUpdateListeners().forEachRemaining(existingListeners::add);
                runtime.getDeploymentService().undeploy(query.deploymentId);
            } catch (EPUndeployException | NullPointerException e) {
                return e.getMessage();
            }

            try {
                CompilerArguments compilerArguments = new CompilerArguments(configuration);
                compilerArguments.getPath().add(runtime.getRuntimePath());
                compiled = EPCompilerProvider.getCompiler().compileQuery(query.query, compilerArguments);
            } catch (EPCompileException e) {
                return e.getMessage();
            }

            try {
                runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId(query.deploymentId));

                for (var listener : existingListeners) {
                    runtime.getDeploymentService().getStatement(query.deploymentId, query.queryStatement).addListener(listener);
                }

            } catch (EPDeployException | IllegalStateException e) {
                return e.getMessage();
            }

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
