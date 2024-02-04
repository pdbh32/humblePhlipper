package phinancialMule;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Sleep;

import static org.dreambot.api.methods.trade.Trade.tradeWithPlayer;
import static org.dreambot.api.methods.trade.Trade.contains;
import static org.dreambot.api.methods.trade.Trade.acceptTrade;
import static org.dreambot.api.methods.trade.Trade.addItem;
import static org.dreambot.api.methods.trade.Trade.isOpen;
import static org.dreambot.api.methods.trade.Trade.close;

public class Muling {
    public String theirName;
    public static Integer totalGp;
    public int trades = 0;
    public void Trade() {
        if (phinancialMule.Main.status == phinancialMule.enums.Status.RECEIVING) {
            if (!Receive()) {
                if (isOpen()) {
                    close();
                }
                return;
            }
        } else {
            if (!Distribute()) {
                if (isOpen()) {
                    close();
                }
                return;
            }
        }
        phinancialMule.Main.action = phinancialMule.enums.Action.IDLE;
    }
    private boolean Receive() {
        if (!Sleep.sleepUntil(() -> tradeWithPlayer(theirName), 5000)) {
            return false;
        }
        if (!Sleep.sleepUntil(() -> contains(false, "Coins"), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        if (!Sleep.sleepUntil(() -> acceptTrade(), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        if (!Sleep.sleepUntil(() -> acceptTrade(), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        trades += 1;
        if (phinancialMule.Main.role == phinancialMule.enums.Role.SLAVE) {
            phinancialMule.Main.status = phinancialMule.enums.Status.FINISHED;
        } else if (trades == phinancialMule.Main.numSlaves) {
            totalGp = Inventory.count("Coins");
            phinancialMule.Main.status = phinancialMule.enums.Status.DISTRIBUTING;
        }
        return true;
    }
    private boolean Distribute() {
        int distribution;
        if (phinancialMule.Main.role == phinancialMule.enums.Role.SLAVE) {
            distribution = Integer.MAX_VALUE;
        } else if (phinancialMule.Main.distributionAmount != null) {
            distribution = phinancialMule.Main.distributionAmount;
        } else {
            distribution = totalGp/(phinancialMule.Main.numSlaves + 1);
        }
        if (!Sleep.sleepUntil(() -> tradeWithPlayer(theirName), 5000)) {
            return false;
        }
        if (!Sleep.sleepUntil(() -> addItem("Coins", distribution), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        if (!Sleep.sleepUntil(() -> acceptTrade(), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        if (!Sleep.sleepUntil(() -> acceptTrade(), 5000)) {
            return false;
        }
        Sleep.sleep(3500,5000);
        trades += 1;
        if (phinancialMule.Main.role == phinancialMule.enums.Role.SLAVE) {
            phinancialMule.Main.status = phinancialMule.enums.Status.RECEIVING;
        } else if (trades == 2 * phinancialMule.Main.numSlaves) {
            phinancialMule.Main.status = phinancialMule.enums.Status.FINISHED;
        }
        return true;
    }

}