namespace EventAnalytics.HostedServices;

public class KafkaSourceHostedService : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        await Task.Delay(TimeSpan.MaxValue, stoppingToken);
    }
}
