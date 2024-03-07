package humblePhlipper.network.webhook;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientProtocol extends humblePhlipper.network.Protocol {
    public ClientProtocol(humblePhlipper.ResourceManager rm) {
        super(rm);
    }
    @Override
    public void go(BufferedReader in, PrintWriter out) {
        out.println(new ClientMessage(rm).CSV());
    }
}
