// Main.java

// Script architecture
import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

// Script functionality
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.container.impl.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Collections;

@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 1.1)
public class Main extends AbstractScript {
    // Parameters
    private static Float timeout = Float.POSITIVE_INFINITY;
    private static Boolean sysExit = false;

    // Constants
    public static final int SLEEP = 600;

    // Variables
    private static final List<Item> itemList = new ArrayList<>();
    private static int startCoins = -1;
    private static LocalDateTime startTime = null;
    private Boolean stopBidding = false; // If true, close bids and sell remaining inventory

    public void onStart(java.lang.String... params) {
        for (String param : params){
            Logger.log(param);
            if (param.startsWith("[") && param.endsWith("]")) {
                String[] optionValue = param.substring(1, param.length() - 1).split(":");
                String option = optionValue[0].trim();
                String value = optionValue[1].trim();
                switch (option) {
                    case "timeout":
                        timeout = Float.parseFloat(value);
                        break;
                    case "sysExit":
                        sysExit = Boolean.parseBoolean(value);
                        break;
                }
            }
        }
        onStart();
    }

    @Override
    public void onStart() {
        if (itemList.isEmpty()) {
            itemList.add(new Item("Logs",1511,15000));
            itemList.add(new Item("Death rune",560,25000));
        }

        if (Client.isLoggedIn()) {
            startCoins = Inventory.count("Coins");
            startTime = LocalDateTime.now();
            Logger.log("Start coins: " + startCoins);
            Logger.log("Start time: " + startTime);
        }
    }
    @Override
    public void onSolverEnd(RandomSolver solver) {
        if (startCoins == -1 && startTime == null) {
            onStart();
        }
    }

    @Override
    public void onPause() {
        stopBidding = !stopBidding;
        if (stopBidding) {
            Logger.log("Press resume to stop bidding");
        }
        else {
            Logger.log("Press resume to resume bidding");
        }
    }

    public void onResume() {
        if (stopBidding) {
            Logger.log("Cancelling bids...");
        } else {
            Logger.log("Making bids...");
        }
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            GrandExchange.goBack();
        }
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            return SLEEP;
        }

        GrandExchange.open();
        Collections.shuffle(itemList);

        // First make requisite updates, cancellations, and collections
        for (Item item : itemList) {
            item.updatePricesOSRSwiki(); // <- 'live' prices, this makes money
            //item.updatePricesDreamBot(); // <- delayed prices, this loses money
            item.checkCancel(stopBidding);
            item.collect();
        }

        // Then attempt to make sell offers
        for (Item item : itemList) {
            item.makeAsk();
        }

        // Finally attempt to make buy offers
        for (Item item : itemList) {
            item.makeBid(stopBidding);
        }

        // Stop bidding timeout condition
        if (Duration.between(startTime, LocalDateTime.now()).toMinutes() >= timeout) {
            if (!stopBidding) {
                Logger.log("Timeout reached, cancelling bids...");
            }
            stopBidding = true;
        }

        // Exit logic
        if (itemList.stream().allMatch(item -> item.sold >= item.targetVol || (stopBidding && item.sold >= item.bought  && item.slot == -1))) {
            return -1;
        }

        return SLEEP;
    }

    @Override
    public void onExit() {
        if (startTime == null) {
            return;
        }

        LocalDateTime endTime  = LocalDateTime.now();
        int endCoins = (Inventory.count("Coins"));

        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + (endCoins - startCoins));
        Logger.log("Trading period of time (PT): " + Duration.between(startTime, endTime));
        Logger.log("--------------------------------------------------------------------------------------");
        String tradeHistory = "\ntime,name,vol,price";
        for (Item item : itemList) {
            Logger.log("\"" + item.name + "\": {\"bought\": " + item.bought + ", \"sold\": " + item.sold + ", \"target\": " + item.targetVol + "},");
            tradeHistory += item.tradeHistory;
        }
        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("<trades>" + tradeHistory + "\n</trades>");
        if (sysExit) {
            System.exit(0);
        }
    }
}