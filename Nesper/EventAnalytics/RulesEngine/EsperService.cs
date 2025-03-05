using com.espertech.esper.common.client.configuration;
using com.espertech.esper.runtime.client;

namespace EventAnalytics.RulesEngine;

public class EsperService
{
    public EsperService(EPRuntime runtime, EPDeployment deployment, Configuration configuration)
    {
        Runtime = runtime;
        Deployment = deployment;
        Configuration = configuration;
    }

    public EPRuntime Runtime { get; set; }
    public EPDeployment Deployment { get; set; }
    public Configuration Configuration { get; set; }
    //public EPCompiled Compiled { get; set; }
}
