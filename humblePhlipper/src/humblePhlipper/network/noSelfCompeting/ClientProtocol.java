package humblePhlipper.network.noSelfCompeting;

import org.dreambot.api.utilities.Sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientProtocol extends humblePhlipper.network.Protocol {
    public ClientProtocol(humblePhlipper.ResourceManager rm) {
        super(rm);
    }
    @Override
    public void go(BufferedReader in, PrintWriter out) throws IOException {
        out.println(new Message(rm).CSV());
        Sleep.sleep(1500);
        rm.session.setNoCompetitionIds(new Message(rm, in.readLine()).tradedIds);
    }
}
