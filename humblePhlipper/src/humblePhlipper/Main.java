package humblePhlipper;

// Script architecture
import org.dreambot.api.Client;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

// Script functionality
import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.bond.Bond;
import org.dreambot.api.methods.tabs.Tabs;
import static org.dreambot.core.Instance.getInstance;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

import Gelox_.DiscordWebhook;

@ScriptManifest(category = Category.MONEYMAKING, name = "humblePhlipper", author = "apnasus", version = 2.76)
public class Main extends AbstractScript {
    public static final ResourceManager rm = new ResourceManager();
    public static final Trading trading = new Trading(rm);
    private static final Paint paint = new Paint(rm);
    private static GUI gui;

    public static final int SLEEP = 1000;
    private static final DecimalFormat commaFormat = new DecimalFormat("#,###");

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
        Logger.log(PlayerSettings.getConfig(1780));
        if (!rm.session.getRunning() || !Client.isLoggedIn()) {
            return SLEEP;
        }

        GrandExchange.open();

        // Auto bond
        if (rm.config.getAutoBond() && PlayerSettings.getConfig(1780) <= 1 && GrandExchange.isOpen()) {
            GrandExchange.cancelAll();
            for (int i=0; i<8; i++) {
                try {
                    trading.Collect(i);
                } catch (Exception e) {
                    if (!rm.config.getDebug()) {
                        continue;
                    }
                    System.err.println("<ERROR>Collect("+i+")</ERROR>");
                    e.printStackTrace();
                }
            }
            if (!Sleep.sleepUntil(() -> GrandExchange.buyItem(13190, 1, (int) Math.round(1.2 * rm.items.get(13190).getLatest().getHigh())), 3000)) {
                Logger.log("<ERROR>Cannot afford bond</ERROR>");
                return -1;
            }
            Sleep.sleep(3000);
            Sleep.sleepUntil(() -> GrandExchange.collect(), 3000);
            Sleep.sleep(3000);
            Sleep.sleepUntil(() -> Bond.redeem(1), 3000);
            Sleep.sleep(3000);
            Tabs.logout();
            Sleep.sleep(3000);
            return SLEEP;
        }

        // Dynamic selection if auto,
        if (rm.config.getAuto()) {
            trading.Select();
        } else {
            trading.Order();
        }

        // Update four hour limits data
        rm.updateFourHourLimits();
        rm.items.updateAllFourLimit();
        rm.items.updateAllTargetVol();

        // Go back if stuck
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            if (Sleep.sleepUntil(GrandExchange::goBack,SLEEP)) {
                Sleep.sleep(SLEEP);
            }
        }

        // Loop through slots and make cancellations
        for (int i=0; i<8; i++) {
            try {
                if (trading.Cancel(i)) { return SLEEP; }
            } catch (Exception e) {
                if (!rm.config.getDebug()) {
                    continue;
                }
                System.err.println("<ERROR>Cancel("+i+")</ERROR>");
                e.printStackTrace();
            }
        }

        // Loop through slots and make collections
        for (int i=0; i<8; i++) {
            try {
                if (trading.Collect(i)) { return SLEEP; }
            } catch (Exception e) {
                if (!rm.config.getDebug()) {
                    continue;
                }
                System.err.println("<ERROR>Collect("+i+")</ERROR>");
                e.printStackTrace();
            }
        }

        // Loop through items and make asks
        for (Integer ID : rm.items.keySet()) {
            try {
                if (trading.MakeAsk(ID)) { return SLEEP; }
            } catch (Exception e) {
                if (!rm.config.getDebug()) {
                    continue;
                }
                System.err.println("<ERROR>MakeAsk("+ID+")</ERROR>");
                e.printStackTrace();
            }
        }

        // Loop through items and make bids
        for (Integer ID : rm.config.getSelections()) {
            try {
                if (trading.MakeBid(ID)) { return SLEEP; }
            } catch (Exception e) {
                if (!rm.config.getDebug()) {
                    continue;
                }
                System.err.println("<ERROR>MakeBid("+ID+")</ERROR>");
                e.printStackTrace();
            }
        }

        // if (Timeout reached) { stop bidding; }
        if ((float) rm.session.getTimer().elapsed() /60000 >= rm.config.getTimeout()) {
            if (rm.session.getBidding()) {
                Logger.log("Timeout or profit cutoff reached, cancelling bids...");
            }
            rm.session.setBidding(false);
        }

        if ((float) rm.session.getTimer().elapsed() /60000 - rm.config.getTimeout() > 15) {
            Logger.log("Timeout exceeded by over 15 minutes, forcing stop...");
            return -1;
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
        rm.disposeSchedulers();
        rm.saveFourHourLimits();
        rm.items.alphabetSort();

        for (humblePhlipper.resources.Items.Item item : rm.items.values()) {
            rm.session.trades.increment(item.getTrades());
        }

        rm.session.incrementSessionHistory("tradesCSV", rm.session.trades.getCSV());
        rm.session.incrementSessionHistory("configJSON", rm.getConfigString());
        String fileName = String.valueOf(LocalDateTime.now()).replaceAll(":","-") + ".json";
        ScriptSettings.save(rm.session.getSessionHistory(), "humblePhlipper", "History", fileName);

        humblePhlipper.resources.data.Trades.Summary allSummary = rm.session.trades.summarise();

        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("<trades>" + rm.session.trades.getCSV() + "\n</trades>");
        Logger.log("--------------------------------------------------------------------------------------");
        Logger.log("Trading over with profit of: " + commaFormat.format(Math.round(allSummary.profit)));
        Logger.log("Runtime: " + Math.round(allSummary.runtimeHours * 60) + " minutes");
        Logger.log("Profit/Hr: " + commaFormat.format(Math.round(allSummary.profit / allSummary.runtimeHours)));
        Logger.log("Errors: " + rm.session.trades.getError());
        Logger.log("--------------------------------------------------------------------------------------");

        String discordWebhook;
        if (rm.config.getDiscordWebhook() != null) {
            discordWebhook = rm.config.getDiscordWebhook();
        } else if (getInstance().getDiscordWebhook() != null) {
            discordWebhook = getInstance().getDiscordWebhook();
        } else {
            discordWebhook = null;
        }
        if (discordWebhook != null) {
            DiscordWebhook webhook = new DiscordWebhook(discordWebhook);
            webhook.setContent((AccountManager.getAccountUsername()) + " ended with profit of " + commaFormat.format(Math.round(rm.session.getProfit())));
            webhook.setAvatarUrl("https://i.postimg.cc/W4DLDmhP/humble-Phlipper.png");
            webhook.setUsername("humblePhlipper");
            webhook.setTts(false);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .addField("Profit", commaFormat.format(Math.round(allSummary.profit)), false)
                    .addField("Runtime", Math.round(allSummary.runtimeHours * 60) + " minutes", false)
                    .addField("Profit/Hr", commaFormat.format(Math.round(allSummary.profit / allSummary.runtimeHours)), false)
                    .addField("Errors", String.valueOf(rm.session.trades.getError()), false));
            try { webhook.execute(); }
            catch(Exception ignored) {}
        }

        if (rm.config.getSysExit()) {
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            EndGUI endGui = new EndGUI();
        });
    }
}