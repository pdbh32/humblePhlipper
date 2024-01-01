// Resources\Items.java

package humblePhlipper.Resources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Items extends LinkedHashMap<Integer, Items.Item> {
    humblePhlipper.ResourceManager rm;
    public Items(humblePhlipper.ResourceManager rm) {
        this.rm = rm;
        for (Integer ID : rm.mappingMap.keySet()) {
            this.put(ID, new Items.Item(ID, rm));
        }
    }
    public void updateAllFourLimit() {
        for (Items.Item item : this.values()) {
            item.updateFourHourLimit();
        }
    }
    public void updateAllTargetVol() {
        for (Items.Item item : this.values()) {
            item.updateTargetVol();
        }
    }
    public void updateAllMapping() {
        for (Items.Item item : this.values()) {
            item.updateMapping();
        }
    }

    public void updateAllLatest() {
        for (Items.Item item : this.values()) {
            item.updateLatest();
        }
    }

    public void updateAllFiveMinute() {
        for (Items.Item item : this.values()) {
            item.updateFiveMinute();
        }
    }

    public void updateAllOneHour() {
        for (Items.Item item : this.values()) {
            item.updateOneHour();
        }
    }
    public void setAllPricing() {
        for (Items.Item item : this.values()) {
            item.setPricing();
        }
    }
    public void updateAllPricing() {
        for (Items.Item item : this.values()) {
            item.updatePricing();
        }
    }

    public class Item {
        private humblePhlipper.ResourceManager rm;
        private int id;
        private humblePhlipper.Resources.SavedData.FourHourLimits.FourHourLimit fourHourLimit;
        private int targetVol;
        private double lastBuyPrice;
        private double profit;
        private int bought;
        private int sold;
        private Integer bid;
        private Integer ask;

        private humblePhlipper.Resources.API.Mapping mapping;
        private humblePhlipper.Resources.API.Latest latest = new humblePhlipper.Resources.API.Latest();
        private humblePhlipper.Resources.API.FiveMinute fiveMinute = new humblePhlipper.Resources.API.FiveMinute();
        private humblePhlipper.Resources.API.OneHour oneHour = new humblePhlipper.Resources.API.OneHour();
        private List<humblePhlipper.Resources.SavedData.History> historyList = new ArrayList<>();

        public Item(int id, humblePhlipper.ResourceManager rm) {
            this.id = id;
            this.rm = rm;

            updateMapping();
            updateLatest();
            updateFiveMinute();
            updateOneHour();

            setPricing();

            updateFourHourLimit();
            updateTargetVol();

            this.bought = 0;
            this.sold = 0;
            this.profit = 0;
        }
        public int getId() {return id; }

        public humblePhlipper.Resources.SavedData.FourHourLimits.FourHourLimit getFourHourLimit() { return fourHourLimit; }
        public void updateFourHourLimit() { this.fourHourLimit = rm.fourHourLimits.get(this.id); }
        public int getTargetVol() { return targetVol; }
        public void updateTargetVol() {
            this.targetVol = (this.mapping.getLimit() != null) ? this.mapping.getLimit() - this.fourHourLimit.getUsedLimit() : Integer.MAX_VALUE;
        }
        public double getLastBuyPrice() { return lastBuyPrice; }

        public void setLastBuyPrice(double lastBuyPrice) {
            this.lastBuyPrice = lastBuyPrice;
        }

        public double getProfit() {
            return profit;
        }

        public void setProfit(double profit) {
            this.profit = profit;
        }

        public int getBought() {
            return bought;
        }

        public void setBought(int bought) {
            this.bought = bought;
        }

        public int getSold() {
            return sold;
        }

        public void setSold(int sold) {
            this.sold = sold;
        }

        public List<humblePhlipper.Resources.SavedData.History> getHistoryList() { return historyList; }

        public void incrementHistoryList(humblePhlipper.Resources.SavedData.History history) { this.historyList.add(history); }

        public Integer getBid() { return bid; }
        public Integer getAsk() { return ask; }

        public humblePhlipper.Resources.API.Mapping getMapping() { return mapping; }
        private void updateMapping() { mapping = rm.mappingMap.get(id); }

        public humblePhlipper.Resources.API.Latest getLatest() { return latest; }
        private void updateLatest() {
            if (rm.latestMap.get(id) == null) {
                return;
            }
            /*if ((rm.latestMap.get(id).getLowTime() != null && this.latest.getLowTime() != null) && rm.latestMap.get(id).getLowTime() <  this.latest.getLowTime()) {
                return;
            }
            if ((rm.latestMap.get(id).getHighTime() != null && this.latest.getHighTime() != null) && rm.latestMap.get(id).getHighTime() <  this.latest.getHighTime()) {
                return;
            }*/
            this.latest = rm.latestMap.get(id);
        }

        public humblePhlipper.Resources.API.FiveMinute getFiveMinute() { return fiveMinute; }
        private void updateFiveMinute() {
            if (rm.fiveMinuteMap.get(id) == null) {
                return;
            }
            this.fiveMinute = rm.fiveMinuteMap.get(id);
        }
        public humblePhlipper.Resources.API.OneHour getOneHour() { return oneHour; }
        private void updateOneHour() {
            if (rm.oneHourMap.get(id) == null) {
                return;
            }
            this.oneHour = rm.oneHourMap.get(id);
        }
        private void setPricing() {
            switch (rm.config.getPricing()) {
                case "latest":
                    this.bid = this.latest.getLow();
                    this.ask = this.latest.getHigh();
                    break;
                case "fiveMinute":
                    this.bid = this.fiveMinute.getAvgLowPrice();
                    this.ask = this.fiveMinute.getAvgHighPrice();
                    break;
                case "oneHour":
                    this.bid = this.oneHour.getAvgLowPrice();
                    this.ask = this.oneHour.getAvgHighPrice();
                    break;
                case "bestOfLatestFiveMinute":
                    this.bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.min(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()) : null;
                    this.ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.min(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()) : null;
                    break;
                case "latestPmOne":
                    this.bid = (this.latest.getLow() != null) ? this.latest.getLow() + 1 : null;
                    this.ask = (this.latest.getHigh() != null) ? this.latest.getHigh() - 1 : null;
                    break;
                case "fiveMinutePmOne":
                    this.bid = (this.fiveMinute.getAvgLowPrice() != null) ? this.fiveMinute.getAvgLowPrice() + 1 : null;
                    this.ask = (this.fiveMinute.getAvgHighPrice() != null) ? this.fiveMinute.getAvgHighPrice() - 1 : null;
                    break;
                case "bestOfLatestPmOneFiveMinutePmOne":
                    this.bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.min(this.latest.getLow() + 1, this.fiveMinute.getAvgLowPrice() + 1) : null;
                    this.ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.max(this.latest.getHigh() - 1, this.fiveMinute.getAvgHighPrice() - 1) : null;
                    break;
            }
        }
        private void updatePricing() {
            switch (rm.config.getPricing()) {
                case "latest":
                    this.bid = this.latest.getLow();
                    this.ask = this.latest.getHigh();
                    break;
                case "fiveMinute":
                    this.bid = (this.fiveMinute.getAvgLowPrice() != null) ? this.fiveMinute.getAvgLowPrice() : this.bid;
                    this.ask = (this.fiveMinute.getAvgHighPrice() != null) ? this.fiveMinute.getAvgHighPrice() : this.ask;
                    break;
                case "oneHour":
                    this.bid = (this.oneHour.getAvgLowPrice() != null) ? this.oneHour.getAvgLowPrice() : this.bid;
                    this.ask = (this.oneHour.getAvgHighPrice() != null) ? this.oneHour.getAvgHighPrice() : this.ask;
                    break;
                case "bestOfLatestFiveMinute":
                    try { this.bid = Math.min(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()); } catch(Exception ignored) {}
                    try { this.ask = Math.min(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()); } catch(Exception ignored) {}
                    break;
                case "latestPmOne":
                    try { this.bid = this.latest.getLow() + 1; } catch(Exception ignored) {}
                    try { this.ask = this.latest.getHigh() - 1; } catch(Exception ignored) {}
                    break;
                case "fiveMinutePmOne":
                    try { this.bid = this.fiveMinute.getAvgLowPrice() + 1; } catch(Exception ignored) {}
                    try { this.ask = this.fiveMinute.getAvgHighPrice() - 1; } catch(Exception ignored) {}
                    break;
                case "bestOfLatestPmOneFiveMinutePmOne":
                    try { this.bid = Math.min(this.latest.getLow() + 1, this.fiveMinute.getAvgLowPrice() + 1); } catch(Exception ignored) {}
                    try { this.ask = Math.max(this.latest.getHigh() - 1, this.fiveMinute.getAvgHighPrice() - 1); } catch(Exception ignored) {}
                    break;
            }
        }
    }
}