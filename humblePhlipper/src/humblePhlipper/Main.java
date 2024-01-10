// Main.java

package humblePhlipper;

// Script architecture
import org.dreambot.api.Client;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

// Script functionality
import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 2.1)
public class Main extends AbstractScript {
    public static final ResourceManager rm = new ResourceManager();
    public static final Trading trading = new Trading(rm);
    private static final Paint paint = new Paint(rm);
    private static GUI gui;

    public static final int SLEEP = 1000;

    @Override
    public void onStart(java.lang.String... params) {
        rm.setQuickStartConfig(params);
        if (rm.config.getAuto()) {
            trading.Select();
        }
        rm.setApiSchedulers();
        rm.setSelectionCSV();
        rm.session.setRunning(true);
        rm.session.setTimer(new Timer());
    }

    @Override
    public void onStart() {
        SwingUtilities.invokeLater(() -> {
            gui = new GUI();
        });
    }

    @Override
    public void onSolverEnd(RandomSolver solver){
        rm.loadFourHourLimits();
        if (Main.rm.config.getAuto() && Main.rm.session.getProfit() == 0) {
            Main.rm.config.setSelections(new LinkedHashSet<Integer>());
            Main.trading.Select();
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if (rm.session.getRunning()) {
            paint.onPaint(g);
        }
    }

    @Override
    public void onPause() {
        rm.session.setBidding(!rm.session.getBidding());
        if (!rm.session.getBidding()) {
            Logger.log("Press resume to stop bidding");
        }
        else {
            Logger.log("Press resume to resume bidding");
        }
    }

    public void onResume() {
        if (!rm.session.getBidding()) {
            Logger.log("Cancelling bids...");
        } else {
            Logger.log("Making bids...");
        }
    }

    @Override
    public int onLoop() {
        if (!rm.session.getRunning() || !Client.isLoggedIn()) {
            return SLEEP;
        }

        GrandExchange.open();

        // Order selections
        trading.Order();

        // Update four hour limits data
        rm.updateFourHourLimits();
        rm.items.updateAllFourLimit();
        rm.items.updateAllTargetVol();

        // Loop through slots and make cancellations
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            trading.Cancel(geItem);
        }

        // Loop through slots and make collections
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            trading.Collect(geItem);
        }

        // Loop through items and make asks
        for (Integer ID : rm.config.getSelections()) {
            if (trading.MakeAsk(ID)) { return SLEEP; }
        }

        // Loop through items and make bids
        for (Integer ID : rm.config.getSelections()) {
            if (trading.MakeBid(ID)) { return SLEEP; }
        }

        // Go back if stuck
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            if (Sleep.sleepUntil(GrandExchange::goBack,SLEEP)) {
                Sleep.sleep(SLEEP);
            }
        }

        // if ((Timeout reached) || (profit cutoff reached)) { stop bidding; }
        if ((float) rm.session.getTimer().elapsed() /60000 >= rm.config.getTimeout() || rm.session.getProfit() >= rm.config.getProfitCutOff()) {
            if (rm.session.getBidding()) {
                Logger.log("Timeout or profit cutoff reached, cancelling bids...");
            }
            rm.session.setBidding(false);
        }

        // if ((GE limits used up) || (not bidding && no inventory)) { Stop(); }
        if (rm.items.values().stream().allMatch(item -> (item.getTargetVol() <= 0 || (!rm.session.getBidding() && item.getSold() >= item.getBought())) && Arrays.stream(GrandExchange.getItems()).noneMatch(geItem -> geItem.getName().equals(item.getMapping().getName())))) {
            return -1;
        }

        return SLEEP;
    }

    @Override
    public void onExit() {
        if (gui != null) {
            gui.Dispose();
        }
        rm.disposeApiSchedulers();
        rm.saveFourHourLimits();
		
		for (humblePhlipper.Resources.Items.Item item : rm.items.values()) {
            for (humblePhlipper.Resources.SavedData.Trade trade : item.getTradeList()) {
                rm.session.incrementTradesCSV("\n" + trade.getCSV());
            }
        }

        rm.session.incrementSessionHistory("tradesCSV", rm.session.getTradesCSV());
        rm.session.incrementSessionHistory("configJSON", rm.getConfigString());
        String fileName = String.valueOf(LocalDateTime.now()).replaceAll(":","-") + ".json";
        ScriptSettings.save(rm.session.getSessionHistory(), "humblePhlipper", "History", fileName);

        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("<trades>" + rm.session.getTradesCSV() + "\n</trades>");
        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + Math.round(rm.session.getProfit()));
        Logger.log("Runtime (minutes): " + (rm.session.getTimer().elapsed()/60000));
        Logger.log("--------------------------------------------------------------------------------------");

        if (rm.config.getSysExit()) {
            System.exit(0);
        }
		
        SwingUtilities.invokeLater(() -> {
            EndGUI endGui = new EndGUI();
        });
    }
}