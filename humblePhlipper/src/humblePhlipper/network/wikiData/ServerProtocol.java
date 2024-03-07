package humblePhlipper.network.wikiData;

import java.io.BufferedReader;
import java.io.PrintWriter;
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
                try {
                    rm.updateLatestMap();
                } catch (Exception e) {
                    request = Request.ERROR;
                }
                break;
            case FIVEMINUTE:
                try {
                    rm.updateFiveMinuteMap();
                } catch (Exception e) {
                    request = Request.ERROR;
                }
                break;
            case ONEHOUR:
                try {
                    rm.updateOneHourMap();
                } catch (Exception e) {
                    request = Request.ERROR;
                }
                break;
        }
    }
    @Override
    public void go(BufferedReader in, PrintWriter out) {
        try {
            out.println(new ServerMessage(rm, request).CSV());
        } catch (Exception e) {
            System.err.println(LocalDateTime.now() + " SERVER write error");
            e.printStackTrace();
        }
    }
}
