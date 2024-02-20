package humblePhlipper.network.wikiData;

import org.dreambot.api.utilities.Logger;

import java.io.IOException;
import java.time.LocalDateTime;

public class ClientProtocol extends humblePhlipper.network.Protocol {
    private final Request request;
    public ClientProtocol(humblePhlipper.ResourceManager rm, Request request) {
        super(rm);
        this.request = request;
    }
    @Override
    public void go() throws IOException {
        try {
            ServerMessage sm = new ServerMessage(rm, request, in.readLine());
            switch (request) {
                case LATEST:
                    rm.latestMap = sm.latestMap;
                    break;
                case FIVEMINUTE:
                    rm.fiveMinuteMap = sm.fiveMinuteMap;
                    break;
                case ONEHOUR:
                    rm.oneHourMap = sm.oneHourMap;
                    break;
            }
            Logger.log("--------------------------------------------");
            Logger.log(LocalDateTime.now() + " CLIENT (updated " + request + " map from server)");
        } catch (Exception e) {
            System.err.println(LocalDateTime.now() + " CLIENT read error");
            e.printStackTrace();
        }
    }
}
