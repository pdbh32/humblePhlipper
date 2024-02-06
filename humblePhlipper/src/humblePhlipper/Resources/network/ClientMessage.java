package humblePhlipper.resources.network;

import org.dreambot.api.utilities.AccountManager;

public class ClientMessage {
    String accountUsername;
    double profit;
    long elapsed;
    public ClientMessage() {
        this.accountUsername = AccountManager.getAccountUsername();
        this.profit = humblePhlipper.Main.rm.session.getProfit();
        this.elapsed = humblePhlipper.Main.rm.session.getTimer().elapsed();
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