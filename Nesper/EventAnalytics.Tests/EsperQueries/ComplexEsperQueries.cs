namespace EventAnalytics.Tests.EsperQueries;

internal class ComplexEsperQueries : AbstractQueries
{
    internal void AddMoreQueries()
    {
        var context = "@public create context ComplexEventContext start @now end after 5 minutes;";
        queries.Add(new LSSQuery(context, "contextCreation"));

        var selectWithinTimeWindow = $@"
                    @name('selectWithinTimeWindowStatement')
                    context ComplexEventContext
                    select window(*) as events from ComplexEvent(value > 45)#length(2);";
        queries.Add(new LSSQuery(selectWithinTimeWindow, "selectWithinTimeWindow"));
    }
}
