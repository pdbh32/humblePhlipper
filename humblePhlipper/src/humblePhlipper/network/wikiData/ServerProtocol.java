package humblePhlipper.network.wikiData;

import org.dreambot.api.utilities.Logger;

import java.time.LocalDateTime;

public class ServerProtocol extends humblePhlipper.network.Protocol {
    Request request;
    public ServerProtocol(humblePhlipper.ResourceManager rm, Request request) {
        super(rm);
        this.request = request;
    }
    @Override
    public void begin() {
        switch(request) {
            case LATEST:
                rm.updateLatestMap();
                break;
            case FIVEMINUTE:
                rm.updateFiveMinuteMap();
                break;
            case ONEHOUR:
                rm.updateOneHourMap();
                break;
        }
        Logger.log(LocalDateTime.now() + " SERVER (updated " + request + " map from Wiki)");
    }
    @Override
    public void go() {
        try {
            out.println(new ServerMessage(rm, request).CSV());
        } catch (Exception e) {
            System.err.println(LocalDateTime.now() + " SERVER write error");
            e.printStackTrace();
        }
    }
}
