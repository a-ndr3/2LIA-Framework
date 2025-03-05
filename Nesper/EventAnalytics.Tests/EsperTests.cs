using System.Diagnostics;
using com.espertech.esper.common.client.configuration;
using com.espertech.esper.compiler.client;
using com.espertech.esper.runtime.client;
using EventAnalytics.Common.Events;
using EventAnalytics.Tests.EsperQueries;
using EventAnalytics.Tests.TestListeners;

namespace EventAnalytics.Tests;

public class EsperTests
{
    private readonly List<ComplexEvent> events;
    private static int uniqueId = 0;
    private readonly EPRuntime runtime;

    // listeners
    private readonly TestListenerTimeWindow testListenerTimeWindow;

    // consts
    private const string ComplexEventName = nameof(ComplexEvent);
    private const string TimeAdvanceEventName = "TimeAdvance";

    // helper methods
    private EPEventService EventService => runtime.EventService;
    private EPDeploymentService DeploymentService => runtime.DeploymentService;
    private static void Log(object obj) => Debug.WriteLine(obj);

    public EsperTests()
    {
        var config = new Configuration();
        config.Common.AddEventType<ComplexEvent>();
        config.Runtime.Threading.IsInternalTimerEnabled = true;

        runtime = EPRuntimeProvider.GetDefaultRuntime(config);
        runtime.Initialize();

        events = new List<ComplexEvent>()
        {
            GetSpecificEvent("Event3", 44.0),
            GetSpecificEvent("Event4", 12.0),
            GetSpecificEvent("Event1", 56.0),
            GetSpecificEvent("Event2", 60.0),
            GetSpecificEvent("Event3", 95.0),
            GetSpecificEvent("Event2", 33.0),
            GetSpecificEvent("Event5", 1.0)
        };

        // register queries
        var queries = new ComplexEsperQueries();
        queries.AddMoreQueries();

        foreach (var query in queries.queries)
        {
            var arguments = new CompilerArguments();
            arguments.Path.Add(runtime.RuntimePath);
            var selectCompiled = EPCompilerProvider.Compiler.Compile(query.Statement, arguments);
            DeploymentService.Deploy(selectCompiled, new DeploymentOptions() { DeploymentId = query.DeploymentId });
        }

        // register listeners
        testListenerTimeWindow = new TestListenerTimeWindow();
        DeploymentService.GetStatement("selectWithinTimeWindow", "selectWithinTimeWindowStatement").AddListener(testListenerTimeWindow);
    }

    [Fact]
    public async Task TestSelectWithinTimeWindow()
    {
        var counter = 0;
        foreach (var cEvent in events)
        {
            Log($"Sending events: {cEvent.eventType} with value: {cEvent.value}");
            EventService.SendEventBean(cEvent, ComplexEventName);
            Log($"Waiting for 5 seconds");
            await WaitFor(5);
            counter++;
            Log("Continue sending events");
            if (counter == 2)
            {
                Assert.Empty(testListenerTimeWindow.EmittedList);
            }
            else if (counter == 4)
            {
                Assert.Equal(2, testListenerTimeWindow.EmittedList.Count);
                Assert.Equal(56.0, testListenerTimeWindow.EmittedList[0].value);
                Assert.Equal(60.0, testListenerTimeWindow.EmittedList[1].value);
            }
            else if (counter == 5)
            {
                Assert.Equal(2, testListenerTimeWindow.EmittedList.Count);
                Assert.Equal(60.0, testListenerTimeWindow.EmittedList[0].value);
                Assert.Equal(95.0, testListenerTimeWindow.EmittedList[1].value);
                return;
            }
        }
    }

    private async Task WaitFor(int seconds)
    {
        Log($"Advancing Esper time by {seconds} seconds...");
        EventService.AdvanceTime(seconds * 1000);
        EventService.SendEventBean(new ComplexEvent() { eventType = TimeAdvanceEventName }, ComplexEventName);

        Log($"Esper time updated. Sleeping for {seconds} seconds.");
        await Task.Delay(seconds * 1000);
    }

    private ComplexEvent GetSpecificEvent(string eventType, double value)
    {
        var random = Random.Shared;
        var mmap = new Dictionary<string, string>();

        var id = Interlocked.Increment(ref uniqueId);

        return new ComplexEvent(id, eventType, value);
    }
}
