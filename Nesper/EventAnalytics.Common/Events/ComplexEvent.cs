namespace EventAnalytics.Common.Events;

#pragma warning disable IDE1006 // Naming Styles

public class ComplexEvent
{
    public ComplexEvent() { }

    public ComplexEvent(int eventId, string eventType, double value)
    {
        this.eventId = eventId;
        this.eventType = eventType;
        this.value = value;
    }

    public int eventId { get; set; }
    public string eventName { get; set; } = string.Empty;
    public string eventType { get; set; } = string.Empty;
    public long timestamp { get; set; }
    public double value { get; set; }
    public float percentage { get; set; }
    public bool IsActive { get; set; }
    public short Level { get; set; }
    public char category { get; set; }
    public byte priority { get; set; }
    public string source { get; set; } = string.Empty;
    public string destination { get; set; } = string.Empty;
    public long duration { get; set; }
    public Dictionary<string, string>? metadata { get; set; }
    public List<int>? dataPoints { get; set; }
}
