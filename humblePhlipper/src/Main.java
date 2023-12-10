// Main.java

// Script architecture
import org.dreambot.api.Client;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

// Script functionality
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Timer;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;


@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 1.1)
public class Main extends AbstractScript {

    // Initialise
    public static Config config = new Config();
    private static Paint paint = new Paint();
    public static API api = new API();
    public static Timer timer;
    public static GUI gui;


    // Constants
    public static final int SLEEP = 600;

    // Variables
    public static Boolean openTheGUI = true;
    public static Boolean isRunning = false;
    public static Boolean bidding = true; // if false, close bids and sell remaining inventory

    @Override
    public void onStart(java.lang.String... params) {
        Main.config.setParams(params);
        openTheGUI = false;
        isRunning = true;
        onStart();
    }

    @Override
    public void onStart() {
        timer = new Timer();
        if (openTheGUI) {
            SwingUtilities.invokeLater(() -> {
                gui = new GUI();
                openTheGUI = false;
            });
        }
    }

    @Override
    public void onPaint(Graphics g) {
        paint.onPaint(g);
    }

    @Override
    public void onPause() {
        bidding = !bidding;
        if (!bidding) {
            Logger.log("Press resume to stop bidding");
        }
        else {
            Logger.log("Press resume to resume bidding");
        }
    }

    public void onResume() {
        if (!bidding) {
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
        api.updateLatest();

        if (!isRunning || !Client.isLoggedIn()) {
            return SLEEP;
        }

        GrandExchange.open();
        Collections.shuffle(config.itemList);

        // First make requisite updates, cancellations, and collections
        for (Item item : config.itemList) {
            item.updatePricesOSRSwiki(); // <- 'live' prices, this makes money
            //item.updatePricesDreamBot(); // <- delayed prices, this loses money
            item.checkCancel();
            item.collect();
        }

        // Then attempt to make sell offers
        for (Item item : config.itemList) {
            item.makeAsk();
        }

        // Finally attempt to make buy offers
        for (Item item : config.itemList) {
            item.makeBid();
        }

        // Stop bidding timeout condition
        if (timer.elapsed()/60000 >= config.timeout) {
            if (bidding) {
                Logger.log("Timeout reached, cancelling bids...");
            }
            bidding = false;
        }

        // Exit logic
        if (config.itemList.stream().allMatch(item -> item.sold >= item.targetVol || (!bidding && item.sold >= item.bought  && item.slot == -1))) {
            return -1;
        }

        return SLEEP;
    }

    @Override
    public void onExit() {
        double profit = 0;
        for (Item item : config.itemList) {
            profit += item.profit;
        }

        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + Math.round(profit));
        Logger.log("Runtime (minutes): " + (timer.elapsed()/60000));
        Logger.log("--------------------------------------------------------------------------------------");
        String tradeHistory = "\ntime,name,vol,price";
        for (Item item : config.itemList) {
            Logger.log("\"" + item.name + "\": {\"bought\": " + item.bought + ", \"sold\": " + item.sold + ", \"target\": " + item.targetVol + "},");
            tradeHistory += item.tradeHistory;
        }
        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("<trades>" + tradeHistory + "\n</trades>");
        if (config.sysExit) {
            System.exit(0);
        }
    }
}