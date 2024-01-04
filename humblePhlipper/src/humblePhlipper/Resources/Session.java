// Resources\Items.java

package humblePhlipper.Resources;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Timer;

import java.time.LocalDateTime;
import java.util.TreeMap;

public class Session {
    private Timer timer;
    private boolean running; // true if config set up
    private boolean bidding; // if false, close bids and sell remaining inventory
    private double profit;
    private Integer startingGp;
    private String historyCSV;
    private TreeMap<Long, Double> timeCumProfitMap; // cumulative profit time series

    public Session() {
        this.timer = new Timer();
        this.running = false;
        this.bidding = true;
        this.profit = 0;
        try { this.startingGp = Inventory.count("Coins"); } catch (Exception ignored) {} // Not logged in
        this.historyCSV = "time,name,vol,price";
        this.timeCumProfitMap = new TreeMap<>();
        this.timeCumProfitMap.put(0L, 0.0);
    }

    // Getter and setter for 'timer'
    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    // Getter and setter for 'isRunning'
    public boolean getRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean getBidding() {
        return bidding;
    }
    public void setBidding(boolean bidding) {
        this.bidding = bidding;
    }

    public double getProfit() {
        return profit;
    }
    public void setProfit(double profit) {
        this.profit = profit;
    }
    public void incrementProfit(double amount) {
        this.profit += amount;
    }

    public Integer getStartingGp() { return startingGp; }
    public void setStartingGp(Integer startingGp) { this.startingGp = startingGp; }

    public TreeMap<Long, Double> getTimeCumProfitMap() {
        return timeCumProfitMap;
    }
    public void setTimeCumProfitMap(TreeMap<Long, Double> timeCumProfitMap) { this.timeCumProfitMap = timeCumProfitMap; }
    public void incrementTimeCumProfitMap(Long time, double amount) { this.timeCumProfitMap.merge(time, amount, Double::sum); }

    public String getHistoryCSV() { return historyCSV; }
    public void setHistoryCSV(String historyCSV) { this.historyCSV = historyCSV; };
    public void incrementHistoryCSV(String historyCSV) { this.historyCSV += historyCSV; }
}
