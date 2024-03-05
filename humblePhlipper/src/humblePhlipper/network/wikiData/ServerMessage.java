package humblePhlipper.network.wikiData;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class ServerMessage {
    private final humblePhlipper.ResourceManager rm;
    private final Request request;
    public Map<Integer, humblePhlipper.resources.wikiObject.Latest> latestMap;
    public Map<Integer, humblePhlipper.resources.wikiObject.FiveMinute> fiveMinuteMap;
    public Map<Integer, humblePhlipper.resources.wikiObject.OneHour> oneHourMap;
    public ServerMessage(humblePhlipper.ResourceManager rm, Request request) {
        this.rm = rm;
        this.request = request;
        switch(request) {
            case LATEST:
                latestMap = rm.latestMap;
                break;
            case FIVEMINUTE:
                fiveMinuteMap = rm.fiveMinuteMap;
                break;
            case ONEHOUR:
                oneHourMap = rm.oneHourMap;
                break;
        }
    }
    public ServerMessage(humblePhlipper.ResourceManager rm, Request request, String message) {
        this.rm = rm;
        this.request = (message.equals("ERROR")) ? Request.ERROR : request;
        switch(request) {
            case LATEST:
                latestMap = rm.gson.fromJson(message, new TypeToken<Map<Integer, humblePhlipper.resources.wikiObject.Latest>>() {}.getType());
                break;
            case FIVEMINUTE:
                fiveMinuteMap = rm.gson.fromJson(message, new TypeToken<Map<Integer, humblePhlipper.resources.wikiObject.FiveMinute>>() {}.getType());
                break;
            case ONEHOUR:
                oneHourMap = rm.gson.fromJson(message, new TypeToken<Map<Integer, humblePhlipper.resources.wikiObject.OneHour>>() {}.getType());
                break;
            case ERROR:
                break;
        }
    }

    public String CSV() {
        switch(request) {
            case LATEST:
                return rm.gson.toJson(latestMap);
            case FIVEMINUTE:
                return rm.gson.toJson(fiveMinuteMap);
            case ONEHOUR:
                return rm.gson.toJson(oneHourMap);
            case ERROR:
                return "ERROR";
        }
        return null;
    }
}
