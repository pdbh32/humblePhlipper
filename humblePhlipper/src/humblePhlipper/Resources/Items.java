// Resources\Items.java

package humblePhlipper.resources;

import humblePhlipper.resources.data.Trades;

import java.util.*;

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

    public void alphabetSort() {
        List<Map.Entry<Integer, Item>> entryList = new ArrayList<>(entrySet());
        entryList.sort(Comparator.comparing(entry -> entry.getValue().mapping.getName()));
        clear();
        for (Map.Entry<Integer, Item> entry : entryList) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public class Item {
        private humblePhlipper.ResourceManager rm;
        private int id;
        private humblePhlipper.resources.data.FourHourLimits.FourHourLimit fourHourLimit;
        private int targetVol;
        private double lastBuyPrice;
        private double profit;
        private int bought;
        private int sold;
        private Integer bid;
        private Integer ask;

        private humblePhlipper.resources.api.Mapping mapping;
        private humblePhlipper.resources.api.Latest latest = new humblePhlipper.resources.api.Latest();
        private humblePhlipper.resources.api.FiveMinute fiveMinute = new humblePhlipper.resources.api.FiveMinute();
        private humblePhlipper.resources.api.OneHour oneHour = new humblePhlipper.resources.api.OneHour();
        public humblePhlipper.resources.data.Trades trades = new humblePhlipper.resources.data.Trades();

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

        public humblePhlipper.resources.data.FourHourLimits.FourHourLimit getFourHourLimit() { return fourHourLimit; }
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

        public humblePhlipper.resources.api.Mapping getMapping() { return mapping; }
        private void updateMapping() { mapping = rm.mappingMap.get(id); }

        public humblePhlipper.resources.api.Latest getLatest() { return latest; }
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

        public humblePhlipper.resources.api.FiveMinute getFiveMinute() { return fiveMinute; }
        private void updateFiveMinute() {
            if (rm.fiveMinuteMap.get(id) == null) {
                return;
            }
            this.fiveMinute = rm.fiveMinuteMap.get(id);
        }
        public humblePhlipper.resources.api.OneHour getOneHour() { return oneHour; }
        private void updateOneHour() {
            if (rm.oneHourMap.get(id) == null) {
                return;
            }
            this.oneHour = rm.oneHourMap.get(id);
        }
        private void setPricing() {
            switch (rm.config.getPricing()) {
                case "latest":
                    this.bid = (this.latest.getLow() != null) ? this.latest.getLow() - rm.config.getPricingOffset() : null;
                    this.ask = (this.latest.getHigh() != null) ? this.latest.getHigh() + rm.config.getPricingOffset() : null;
                    break;
                case "fiveMinute":
                    this.bid = (this.fiveMinute.getAvgLowPrice() != null) ? this.fiveMinute.getAvgLowPrice() - rm.config.getPricingOffset() : null;
                    this.ask = (this.fiveMinute.getAvgHighPrice() != null) ? this.fiveMinute.getAvgHighPrice() + rm.config.getPricingOffset() : null;
                    break;
                case "oneHour":
                    this.bid = (this.oneHour.getAvgLowPrice() != null) ? this.oneHour.getAvgLowPrice() - rm.config.getPricingOffset() : null;
                    this.ask = (this.oneHour.getAvgHighPrice() != null) ? this.oneHour.getAvgHighPrice() + rm.config.getPricingOffset() : null;
                    break;
                case "bestOfLatestFiveMinute":
                    this.bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.min(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()) - rm.config.getPricingOffset() : null;
                    this.ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.max(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()) + rm.config.getPricingOffset() : null;
                    break;
                case "worstOfLatestFiveMinute":
                    this.bid = (this.latest.getLow() != null && this.fiveMinute.getAvgLowPrice() != null) ? Math.max(this.latest.getLow(), this.fiveMinute.getAvgLowPrice()) - rm.config.getPricingOffset() : null;
                    this.ask = (this.latest.getHigh() != null && this.fiveMinute.getAvgHighPrice() != null) ? Math.min(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice()) + rm.config.getPricingOffset() : null;
                    break;
            }
        }
        private void updatePricing() {
            switch (rm.config.getPricing()) {
                case "latest":
                    try { this.bid = this.latest.getLow() - rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    try { this.ask = this.latest.getHigh() + rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    break;
                case "fiveMinute":
                    try { this.bid = this.fiveMinute.getAvgLowPrice() - rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    try { this.ask = this.fiveMinute.getAvgHighPrice() + rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    break;
                case "oneHour":
                    try { this.bid = this.oneHour.getAvgLowPrice() - rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    try { this.ask = this.oneHour.getAvgHighPrice() + rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    break;
                case "bestOfLatestFiveMinute":
                    try { this.bid = Math.min(this.latest.getLow(), this.fiveMinute.getAvgLowPrice())  - rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    try { this.ask = Math.max(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice())  + rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    break;
                case "worstOfLatestFiveMinute":
                    try { this.bid = Math.max(this.latest.getLow(), this.fiveMinute.getAvgLowPrice())  - rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    try { this.ask = Math.min(this.latest.getHigh(), this.fiveMinute.getAvgHighPrice())  + rm.config.getPricingOffset(); } catch(Exception ignored) {}
                    break;
            }
        }
    }
}