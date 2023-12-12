// Main.java

// Script architecture
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

// Script functionality
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 1.10)
public class Main extends AbstractScript {

    // Initialise
    public static Config config = new Config();
    public static API api = new API();
    public static Timer timer;
    public static GUI gui;

    private static final Paint paint = new Paint();

    // Constants
    public static final int SLEEP = 1000;

    // Variables
    public static Boolean openTheGUI = true; // false if quickstart
    public static Boolean isRunning = false; // true if config set up
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
        if (isRunning) {
            paint.onPaint(g);
        }
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
    }

    @Override
    public int onLoop() {
        api.updateLatest();

        if (!isRunning || !Client.isLoggedIn()) {
            return SLEEP;
        }

        GrandExchange.open();

        // Recreate itemMap with shuffled values for the sake of randomising order of asks/bids
        List<Item> itemList = new ArrayList<>(config.itemMap.values());
        Collections.shuffle(itemList);
        config.itemMap.clear();
        itemList.forEach(item -> config.itemMap.put(item.id, item));

        // Loop through slots and make cancellations
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            if ((geItem.isBuyOffer() && (geItem.getPrice() != api.latestMap.get(geItem.getID()).getLow() ||
                    0.99 * api.latestMap.get(geItem.getID()).getHigh() - api.latestMap.get(geItem.getID()).getLow() <= 0 ||
                    Widgets.get(465, 7 + geItem.getSlot(), 22).getWidth() > 0 ||
                    !Main.bidding)) ||
                    (geItem.isSellOffer() && geItem.getPrice() != api.latestMap.get(geItem.getID()).getHigh())) {
                if (Sleep.sleepUntil(() -> GrandExchange.cancelOffer(geItem.getSlot()), SLEEP)) {
                    Sleep.sleep(SLEEP);
                }
            }
        }

        // Loop through slots and make collections
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            boolean collectionSuccess = false;
            boolean isBuy = geItem.isBuyOffer();
            int vol = geItem.getTransferredAmount();
            double price = 0;

            if (geItem.isReadyToCollect()) {
                if (!Sleep.sleepUntil(() -> GrandExchange.openSlotInterface(geItem.getSlot()), SLEEP)) {
                    continue;
                } else {
                    Sleep.sleep(SLEEP);
                }

                // Unfortunately .getTransferredValue() shows pre-tax gold for sales and we can't ascertain post-tax gold from this.
                // We therefore have to work it out manually by check widgets.

                if (isBuy) {
                    price = (double) -1 * geItem.getTransferredValue()/vol;
                } else {
                    double coinsCollected= 0;
                    if (GrandExchange.getOfferSecondItemWidget().getItemId() == 995 && !GrandExchange.getOfferSecondItemWidget().isHidden()) {
                        coinsCollected = GrandExchange.getOfferSecondItemWidget().getItemStack();
                    } else if (GrandExchange.getOfferFirstItemWidget().getItemId() == 995) {
                        coinsCollected = GrandExchange.getOfferFirstItemWidget().getItemStack();
                    }
                    price = coinsCollected/vol;
                }

                if (!GrandExchange.getOfferSecondItemWidget().isHidden()) {
                    if (Sleep.sleepUntil(() -> GrandExchange.getOfferSecondItemWidget().interact(), SLEEP)) {
                        Sleep.sleep(SLEEP);
                        if (Sleep.sleepUntil(() -> GrandExchange.getOfferFirstItemWidget().interact(), SLEEP)) {
                            Sleep.sleep(SLEEP);
                            collectionSuccess = true;
                        }
                    }
                } else {
                    if (Sleep.sleepUntil(() -> GrandExchange.getOfferFirstItemWidget().interact(), SLEEP)) {
                        Sleep.sleep(SLEEP);
                        collectionSuccess = true;
                    }
                }

                if (collectionSuccess && vol != 0) {
                    if (isBuy) {
                        config.itemMap.get(geItem.getID()).bought += vol;
                        config.itemMap.get(geItem.getID()).lastBuyPrice = -1 * price;
                    }
                    else {
                        config.itemMap.get(geItem.getID()).sold += vol;
                        config.itemMap.get(geItem.getID()).profit += (price - config.itemMap.get(geItem.getID()).lastBuyPrice) * vol;
                    }
                    config.itemMap.get(geItem.getID()).tradeHistory += "\n"+ LocalDateTime.now()+","+geItem.getName()+","+vol+","+price;
                    Logger.log("<trade>\n"+ LocalDateTime.now()+","+geItem.getName()+","+vol+","+price+"</trade>");
                }
            }
        }

        // Loop through items and make asks
        for (Item item : config.itemMap.values()) {
            if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getName().equals(item.name))) {
                continue;
            }
            if (item.sold >= item.bought || Inventory.count(item.name) == 0) {
                continue;
            }

            if (Sleep.sleepUntil(() -> GrandExchange.sellItem(item.name, (item.bought - item.sold), api.latestMap.get(item.id).getHigh()), SLEEP)) {
                Sleep.sleep(SLEEP);
            }
        }

        // Loop through items and make bids
        for (Item item : config.itemMap.values()) {
            if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getName().equals(item.name))) {
                continue;
            }
            if (item.bought >= item.targetVol || !Main.bidding || Inventory.count("Coins") < api.latestMap.get(item.id).getLow() || 0.99 * api.latestMap.get(item.id).getHigh() - api.latestMap.get(item.id).getLow() <= 0) {
                continue;
            }
            if (Sleep.sleepUntil(() -> GrandExchange.buyItem(item.name, (int) Math.min((int) Math.min(Math.floor((double) Inventory.count("Coins") /api.latestMap.get(item.id).getLow()), (item.targetVol - item.bought)), Math.floor((double) (Main.config.maxBidVol * item.targetVol) /100)), api.latestMap.get(item.id).getLow()), SLEEP)) {
                Sleep.sleep(SLEEP);
            }
        }

        // Go back if stuck
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            if (Sleep.sleepUntil(GrandExchange::goBack,SLEEP)) {
                Sleep.sleep(SLEEP);
            }
        }

        // Stop bidding timeout condition
        if (timer.elapsed()/60000 >= config.timeout) {
            if (bidding) {
                Logger.log("Timeout reached, cancelling bids...");
            }
            bidding = false;
        }

        // Exit logic
        if (config.itemMap.values().stream().allMatch(item -> item.sold >= item.targetVol || (!bidding && item.sold >= item.bought && Arrays.stream(GrandExchange.getItems()).noneMatch(geItem -> geItem.getName().equals(item.name))))) {
            return -1;
        }

        return SLEEP;
    }

    @Override
    public void onExit() {
        double profit = 0;
        for (Item item : config.itemMap.values()) {
            profit += item.profit;
        }

        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + Math.round(profit));
        Logger.log("Runtime (minutes): " + (timer.elapsed()/60000));
        Logger.log("--------------------------------------------------------------------------------------");
        String tradeHistory = "\ntime,name,vol,price";
        for (Item item : config.itemMap.values()) {
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