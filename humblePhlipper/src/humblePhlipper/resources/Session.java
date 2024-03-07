package humblePhlipper.resources;

import org.dreambot.api.utilities.Timer;

import java.util.*;

public class Session {
    private Timer timer;
    private boolean running; // true if config set up
    private boolean bidding; // if false, close bids and sell remaining inventory
    private double profit;
    public humblePhlipper.resources.data.Trades trades;
    private TreeMap<Long, Double> timeCumProfitMap; // cumulative profit time series
    private Map<String, String> sessionHistory; // to save down history
    private Set<Integer> noCompetitionIds;

    public Session() {
        this.timer = new Timer();
        this.running = false;
        this.bidding = true;
        this.profit = 0;
        this.trades = new humblePhlipper.resources.data.Trades();
        this.timeCumProfitMap = new TreeMap<>();
        this.timeCumProfitMap.put(0L, 0.0);
        this.sessionHistory = new HashMap<>();
        this.noCompetitionIds = new HashSet<>();
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

    public TreeMap<Long, Double> getTimeCumProfitMap() {
        return timeCumProfitMap;
    }
    public void setTimeCumProfitMap(TreeMap<Long, Double> timeCumProfitMap) { this.timeCumProfitMap = timeCumProfitMap; }
    public void incrementTimeCumProfitMap(Long time, double amount) { this.timeCumProfitMap.merge(time, amount, Double::sum); }

    public Map<String, String> getSessionHistory() { return sessionHistory; }
    public void setSessionHistory(Map<String, String> sessionHistory) { this.sessionHistory = sessionHistory; }
    public void incrementSessionHistory(String key, String value) { this.sessionHistory.put(key, value); }

    public Set<Integer> getNoCompetitionIds() { return noCompetitionIds; }
    public void setNoCompetitionIds(Set<Integer> noCompetitionIds) { this.noCompetitionIds = noCompetitionIds; }
}
