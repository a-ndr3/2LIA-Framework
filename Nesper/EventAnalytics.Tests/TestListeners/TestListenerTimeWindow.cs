using com.espertech.esper.compat.collections;
using com.espertech.esper.runtime.client;
using EventAnalytics.Common.Events;

namespace EventAnalytics.Tests.TestListeners;

internal class TestListenerTimeWindow : UpdateListener
{
    public List<ComplexEvent> EmittedList = new();

    public void Update(object sender, UpdateEventArgs eventArgs)
    {
        EmittedList.Clear();
        foreach (var newEvent in eventArgs.NewEvents)
        {
            var @event = (HashMap<string, object>)newEvent.Underlying;
            var events = (ComplexEvent[])@event.Get("events");
            EmittedList.AddRange(events);
        }
    }
}
