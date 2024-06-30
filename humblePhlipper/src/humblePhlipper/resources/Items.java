package humblePhlipper.resources;

import humblePhlipper.ResourceManager;
import humblePhlipper.resources.data.FourHourLimits;
import humblePhlipper.resources.data.Trades;
import humblePhlipper.resources.wikiObject.FiveMinute;
import humblePhlipper.resources.wikiObject.Latest;
import humblePhlipper.resources.wikiObject.Mapping;
import humblePhlipper.resources.wikiObject.OneHour;

import java.util.*;

public class Items extends LinkedHashMap<Integer, Items.Item> {
    ResourceManager rm;
    public Items(ResourceManager rm) {
        this.rm = rm;
        for (Integer ID : rm.mappingMap.keySet()) {
            this.put(ID, new Item(ID, rm));
        }
    }
    public void updateAllFourLimit() {
        for (Item item : this.values()) {
            item.updateFourHourLimit();
        }
    }
    public void updateAllTargetVol() {
        for (Item item : this.values()) {
            item.updateTargetVol();
        }
    }
    public void updateAllMapping() {
        for (Item item : this.values()) {
            item.updateMapping();
        }
    }

    public void updateAllLatest() {
        for (Item item : this.values()) {
            item.updateLatest();
        }
    }

    public void updateAllFiveMinute() {
        for (Item item : this.values()) {
            item.updateFiveMinute();
        }
    }

    public void updateAllOneHour() {
        for (Item item : this.values()) {
            item.updateOneHour();
        }
    }
    public void updateAllPricing() {
        for (Item item : this.values()) {
            item.updatePricing();
        }
    }

    public void alphabetSort() {
        List<Map.Entry<Integer, Item>> entryList = new ArrayList<>(entrySet());
        entryList.sort(Comparator.comparing(entry -> entry.getValue().mapping.getName()));
        clear();
        for (Map.Entry<Integer, Item> entry : entryList) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public class Item {
        private ResourceManager rm;
        private int id;
        private FourHourLimits.FourHourLimit fourHourLimit;
        private int targetVol;
        private double lastBuyPrice;
        private double profit;
        private int bought;
        private int sold;
        private Integer bid;
        private Integer ask;

        private Mapping mapping;
        private Latest latest = new Latest();
        private FiveMinute fiveMinute = new FiveMinute();
        private OneHour oneHour = new OneHour();
        public Trades trades = new Trades();

        public Item(int id, ResourceManager rm) {
            this.id = id;
            this.rm = rm;

            updateMapping();
            updateLatest();
            updateFiveMinute();
            updateOneHour();

            updatePricing();

            updateFourHourLimit();
            updateTargetVol();

            this.bought = 0;
            this.sold = 0;
            this.profit = 0;
        }
        public int getId() {return id; }

        public FourHourLimits.FourHourLimit getFourHourLimit() { return fourHourLimit; }
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
        public Trades getTrades() { return trades; }
        public Integer getBid() { return bid; }
        public Integer getAsk() { return ask; }

        public Mapping getMapping() { return mapping; }
        private void updateMapping() { mapping = rm.mappingMap.get(id); }

        public Latest getLatest() { return latest; }
        private void updateLatest() {
            if (rm.latestMap.get(id) == null) {
                return;
            }
            this.latest = rm.latestMap.get(id);
        }

        public FiveMinute getFiveMinute() { return fiveMinute; }
        private void updateFiveMinute() {
            if (rm.fiveMinuteMap.get(id) == null) {
                return;
            }
            this.fiveMinute = rm.fiveMinuteMap.get(id);
        }
        public OneHour getOneHour() { return oneHour; }
        private void updateOneHour() {
            if (rm.oneHourMap.get(id) == null) {
                this.oneHour = new OneHour(); // update vol to default (zero)
                return;
            }
            this.oneHour = rm.oneHourMap.get(id);
        }
        private void updatePricing() {
            Integer bid = null;
            Integer ask = null;
            switch (rm.config.getPricing()) {
                case "latest":
                    bid = (this.latest.getLow() != null) ? this.latest.getLow() : null;
                    ask = (this.latest.getHigh() != null) ? this.latest.getHigh() : null;
                    break;
                case "fiveMinute":
                    bid = (this.fiveMinute.getAvgLowPrice() != null) ? this.fiveMinute.getAvgLowPrice() : null;
                    ask = (this.fiveMinute.getAvgHighPrice() != null) ? this.fiveMinute.getAvgHighPrice() : null;
                    break;
                case "oneHour":
                    bid = (this.oneHour.getAvgLowPrice() != null) ? this.oneHour.getAvgLowPrice() : null;
                    ask = (this.oneHour.getAvgHighPrice() != null) ? this.oneHour.getAvgHighPrice() : null;
                    break;
                case "bestOfLatestFiveMinute":
                    bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.min(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()) : null;
                    ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.max(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()) : null;
                    break;
                case "worstOfLatestFiveMinute":
                    bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.max(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()) : null;
                    ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.min(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()) : null;
                    break;
                case "bestOfLatestOneHour":
                    bid = (this.latest.getLow() != null && this.oneHour.getAvgLowPrice() != null) ? Math.min(this.latest.getLow(), this.oneHour.getAvgLowPrice()) : null;
                    ask = (this.latest.getHigh() != null && this.oneHour.getAvgHighPrice() != null) ? Math.max(this.latest.getHigh(), this.oneHour.getAvgHighPrice()) : null;
                    break;
            }

            if (bid == null && this.latest.getLow() != null) { bid = this.latest.getLow(); }
            if (ask == null && this.latest.getHigh() != null) { ask = this.latest.getHigh(); }

            if (bid == null || ask == null) { return; }

            Integer pricingOffset;
            if (rm.config.getPricingOffsetAsPercentage()) {
                double profitMargin = Math.max(Math.ceil(0.99 * ask), ask - 5000000) - bid;
                pricingOffset = (int) (0.01 * rm.config.getPricingOffset() * profitMargin);
            } else {
                pricingOffset = rm.config.getPricingOffset();
            }

            this.bid = bid - pricingOffset;
            this.ask = ask + pricingOffset;

            if (this.bid < 0) { this.bid = null; }
            if (this.ask < 0) { this.ask = 1; }
        }
    }
}