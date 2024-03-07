package humblePhlipper.network.noSelfCompeting;

import org.dreambot.api.utilities.Sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class ServerProtocol extends humblePhlipper.network.Protocol {
    private final Set<Integer> noCompetitionIds = new HashSet<>();
    public ServerProtocol(humblePhlipper.ResourceManager rm) {
        super(rm);
        noCompetitionIds.addAll(new Message(rm).tradedIds);
    }
    @Override
    public void go(BufferedReader in, PrintWriter out) throws IOException {
        noCompetitionIds.addAll(new Message(rm, in.readLine()).tradedIds);
        Sleep.sleep(1500);
        out.println(new Message(rm, noCompetitionIds).CSV());
    }

    @Override
    public void finish() {
        rm.session.setNoCompetitionIds(noCompetitionIds);
    }
}
