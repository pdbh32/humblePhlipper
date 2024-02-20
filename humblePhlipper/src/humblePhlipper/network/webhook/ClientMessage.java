package humblePhlipper.network.webhook;

import org.dreambot.api.utilities.AccountManager;

public class ClientMessage {
    String accountUsername;
    double profit;
    long elapsed;
    public ClientMessage(humblePhlipper.ResourceManager rm) {
        this.accountUsername = AccountManager.getAccountUsername();
        this.profit = rm.session.getProfit();
        this.elapsed = rm.session.getTimer().elapsed();
    }
    public ClientMessage(String message) {
        String[] components = message.split(",");
        this.accountUsername = components[0];
        this.profit = Double.parseDouble(components[1]);
        this.elapsed = Long.parseLong(components[2]);
    }

    public String CSV() {
        return this.accountUsername + "," + this.profit + "," + this.elapsed;
    }
}
