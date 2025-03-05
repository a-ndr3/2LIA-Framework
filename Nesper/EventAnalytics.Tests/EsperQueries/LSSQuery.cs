namespace EventAnalytics.Tests.EsperQueries;

internal class LSSQuery
{
    public LSSQuery(string statement, string deployment)
    {
        Statement = statement;
        DeploymentId = deployment;
    }

    public string Statement { get; set; }

    public string DeploymentId { get; set; }
}
