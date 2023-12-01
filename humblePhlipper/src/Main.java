// Main.java

// Script architecture
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.randoms.RandomSolver;

// Script functionality
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.container.impl.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Collections;

@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 1.0)
public class Main extends AbstractScript {

    private static final List<Item> itemList = new ArrayList<>();
    private static int startCoins;
    private static LocalDateTime startTime;

    @Override
    public void onStart() {
        itemList.add(new Item("Logs",1511,15000));
        itemList.add(new Item("Death rune",560,25000));

        startCoins = Inventory.count("Coins");
        startTime = LocalDateTime.now();
    }
    @Override
    public void onSolverEnd(RandomSolver solver){
        startCoins = Inventory.count("Coins");
        startTime = LocalDateTime.now();
    }

    @Override
    public int onLoop() {

        GrandExchange.open();
        Collections.shuffle(itemList);

        // First make requisite updates, cancellations, and collections
        for (Item item : itemList) {
            item.updatePricesOSRSwiki(); // <- 'live' prices, this makes money
            //item.updatePricesDreamBot(); // <- delayed prices, this loses money
            item.checkCancel();
            item.collect();
        }

        // Then attempt to make sell offers
        for (Item item : itemList) {
            item.makeAsk();
        }

        // Finally attempt to make buy offers
        for (Item item : itemList) {
            item.makeBid();
        }

        // Exit logic
        boolean tradingOver = true;
        for (Item item : itemList) {
            if (!(item.bought >= item.targetVol  &&  Inventory.count(item.name) == 0 && item.slot == -1))  {
                tradingOver = false;
                break;
            }
        }
        if (tradingOver) {
            return -1;
        }

        return 500;
    }

    @Override
    public void onExit() {
        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + (Inventory.count("Coins") - startCoins));
        Logger.log("Trading period of time (PT): " + Duration.between(startTime, LocalDateTime.now()));
        Logger.log("--------------------------------------------------------------------------------------");
        for (Item item : itemList) {
            Logger.log( "Traded " + item.bought + " " + item.name + " with trade history CSV:" + item.tradeHistory);
        }
    }
}