package com.espertech;

import com.espertech.AnalysisCore.AnalysisListenerFactory;
import com.espertech.AnalysisCore.IssueTopicHelper;
import com.espertech.ESPERQueries.EsperQueryDTO;
import com.espertech.ESPERQueries.QueryFactory;
import com.espertech.PrometheusMetrics.PrometheusMetrics;
import com.espertech.QueriesDatabase.QueryMetadataDTO;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.internal.collection.Pair;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    public void deployAnalysisQueries(Collection<QueryMetadataDTO> queries) {
        for (var q : queries) {
            try {
                var dto = this.deployNewQueryNoDefaultListener(q.query, "AnalysisQuery" + q.query.length(), List.of("SpanEvent"), "System", "SpanEvent");
                var deployment = this.getDeployment().getDeployment(dto.deploymentId);
                Arrays.stream(deployment.getStatements()).findAny()
                        .filter(stmt -> stmt.getName().equals(dto.queryStatement))
                        .ifPresent(stmt -> {
                            stmt.addListener(AnalysisListenerFactory.createListenerForQuery(IssueTopicHelper.fromString(q.category), stmt.getName(), q.description.equals("1")));
                            PrometheusMetrics.analysisQueriesDeployed.inc();
                            Main.logger.info("Deployed query '{}' for topic {} with an auto-attached listener", stmt.getName(), q.category);
                        });
            } catch (Exception e) {
                e.printStackTrace();
                PrometheusMetrics.queryDeploymentErrorsTotal.labels(q.name).inc();
            }
        }
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
