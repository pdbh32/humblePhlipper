package humblePhlipper.network.webhook;

import humblePhlipper.Main;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
public class ClientProtocol extends humblePhlipper.network.Protocol {
    public ClientProtocol(humblePhlipper.ResourceManager rm) {
        super(rm);
    }
    @Override
    public void go() {
        out.println(new ClientMessage(rm).CSV());
    }
}
