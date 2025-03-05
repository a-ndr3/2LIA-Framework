namespace EventAnalytics.HostedServices;

public class RulesEngineHostedService : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        await Task.Delay(TimeSpan.MaxValue, stoppingToken);
    }
}
