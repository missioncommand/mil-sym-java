package armyc2.c5isr.renderer.utilities;

public class SettingsChangedEvent extends Throwable {

    static public final String EventType_CacheSizeChanged = "CACHE_CHANGED";
    static public final String EventType_CacheToggled = "CACHE_TOGGLED";
    static public final String EventType_FontChanged = "FONT_CHANGED";

    
    private String _EventType = null;
    public SettingsChangedEvent(String eventType)
    {
        if(eventType != null && eventType.equals("") == false)
        {
            _EventType = eventType;
        }
    }
    
    public String getEventType()
    {
        return _EventType;
    }

}
