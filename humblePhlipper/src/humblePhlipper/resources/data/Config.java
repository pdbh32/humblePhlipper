package humblePhlipper.resources.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashSet;
import java.util.Set;

public class Config {
    // Miscellaneous
    @SerializedName("timeout")
    @Expose
    private Float timeout;
    @SerializedName("sysExit")
    @Expose
    private Boolean sysExit;
    @SerializedName("discordWebhook")
    @Expose
    private String discordWebhook;
    @SerializedName("profitCutOff")
    @Expose
    private Integer profitCutOff;
    @SerializedName("debug")
    @Expose
    private Boolean debug;

    // Bid Restrictions
    @SerializedName("maxBidValue")
    @Expose
    private Integer maxBidValue;
    @SerializedName("maxBidVol")
    @Expose
    private Integer maxBidVol;

    // Bid Priority
    @SerializedName("priorityProfit")
    @Expose
    private Integer priorityProfit;
    @SerializedName("priorityVol")
    @Expose
    private Integer priorityVol;
    @SerializedName("priorityCapitalBinding")
    @Expose
    private Integer priorityCapitalBinding;

    // Pricing
    @SerializedName("pricing")
    @Expose
    private String pricing;
    @SerializedName("pricingOffset")
    @Expose
    private Integer pricingOffset;
    @SerializedName("apiInterval")
    @Expose
    private Integer apiInterval;

    @SerializedName("bandwidthSaver")
    @Expose
    private Boolean bandwidthSaver;

    // Selections
    @SerializedName("selections")
    @Expose
    private Set<Integer> selections;

    // Auto Selections
    @SerializedName("auto")
    @Expose
    private Boolean auto;
    @SerializedName("minVol")
    @Expose
    private Integer minVol;
    @SerializedName("maxBidAskVolRatio")
    @Expose
    private Float maxBidAskVolRatio;
    @SerializedName("minMargin")
    @Expose
    private Integer minMargin;
    @SerializedName("maxBidPrice")
    @Expose
    private Integer maxBidPrice;
    @SerializedName("tradeRestricted")
    @Expose
    private Boolean tradeRestricted;
    @SerializedName("members")
    @Expose
    private Boolean members;
    @SerializedName("numToSelect")
    @Expose
    private Integer numToSelect;

    public Config() {
        // Miscellaneous
        this.timeout = 240F;
        this.profitCutOff = Integer.MAX_VALUE;
        this.sysExit = false;
        this.discordWebhook = null;
        this.debug = false;

        // Bid Restrictions
        this.maxBidValue = 1000000;
        this.maxBidVol = 20000;

        // Bid Priority
        this.priorityProfit = 100;
        this.priorityVol = 100;
        this.priorityCapitalBinding = 0;

        // Pricing
        this.pricing = "bestOfLatestFiveMinute";
        this.pricingOffset = -1;
        this.apiInterval = 1;
        this.bandwidthSaver = false;

        // Selections
        this.selections = new LinkedHashSet<>();
        this.selections.add(1511); // Logs
        this.selections.add(560); // Death rune

        // Auto Selections
        this.auto = true;
        this.minVol = 10000;
        this.minMargin = -10;
        this.maxBidPrice = 20000;
        this.maxBidAskVolRatio = 3.0f;
        this.tradeRestricted = false;
        this.members = false;
        this.numToSelect = 512;
    }

    // Miscellaneous Getters
    public Float getTimeout() {
        return timeout;
    }
    public Integer getProfitCutOff() { return profitCutOff; }
    public Boolean getSysExit() {
        return sysExit;
    }
    public String getDiscordWebhook() { return discordWebhook; }
    public Boolean getDebug() {
        return debug;
    }

    // Bid Restrictions Getters
    public Integer getMaxBidValue() {
        return maxBidValue;
    }
    public Integer getMaxBidVol() {
        return maxBidVol;
    }

    // Bid Priority Getters
    public Integer getPriorityProfit() {
        return priorityProfit;
    }
    public Integer getPriorityVol() {
        return priorityVol;
    }
    public Integer getPriorityCapitalBinding() {
        return priorityCapitalBinding;
    }

    // Pricing Getters
    public String getPricing() {
        return pricing;
    }
    public Integer getPricingOffset() {
        return pricingOffset;
    }
    public Integer getApiInterval() {
        return apiInterval;
    }
    public Boolean getBandwidthSaver() { return bandwidthSaver; }

    // Selections Getters
    public Set<Integer> getSelections() {
        return selections;
    }

    // Auto Selections Getters
    public Boolean getAuto() {
        return auto;
    }

    public Integer getMinVol() {
        return minVol;
    }

    public Float getMaxBidAskVolRatio() {
        return maxBidAskVolRatio;
    }

    public Integer getMinMargin() { return minMargin; }

    public Integer getMaxBidPrice() { return maxBidPrice; }

    public Boolean getTradeRestricted() { return tradeRestricted; }

    public Boolean getMembers() { return members; }

    public Integer getNumToSelect() { return numToSelect; }

    // Miscellaneous Setters
    public void setTimeout(Float timeout) { this.timeout = timeout; }
    public void setProfitCutOff(Integer profitCutoff) { this.profitCutOff = profitCutoff; }
    public void setSysExit(Boolean sysExit) { this.sysExit = sysExit; }
    public void setDiscordWebhook(String discordWebhook ) { this.discordWebhook = discordWebhook; }
    public void setDebug(Boolean debug) { this.debug = debug; }

    // Bid Restrictions Setters
    public void setMaxBidValue(Integer maxBidValue) { this.maxBidValue = maxBidValue; }
    public void setMaxBidVol(Integer maxBidVol) { this.maxBidVol = maxBidVol; }

    // Bid Priority Setters
    public void setPriorityProfit(Integer priorityProfit) {
        if (priorityProfit >= 0 && priorityProfit <= 100) {
            this.priorityProfit = priorityProfit;
        }
    }
    public void setPriorityVol(Integer priorityVol) {
        if(priorityVol >= 0 && priorityVol <= 100) {
            this.priorityVol = priorityVol;
        }
    }
    public void setPriorityCapitalBinding(Integer priorityCapitalBinding) {
        if(priorityCapitalBinding >= 0 && priorityCapitalBinding <= 100) {
            this.priorityCapitalBinding = priorityCapitalBinding;
        }
    }

    // Pricing Setters
    public void setPricing(String pricing) {
        this.pricing = pricing;
    }
    public void setPricingOffset(Integer pricingOffset) {
        this.pricingOffset = pricingOffset;
    }
    public void setApiInterval(Integer apiInterval) {
        if (apiInterval >= 1 && apiInterval <= 300) {
            this.apiInterval = apiInterval;
        }
    }
    public void setBandwidthSaver(Boolean bandwidthSaver) { this.bandwidthSaver = bandwidthSaver; }

    // Selections Setters
    public void setSelections(Set<Integer> selections) { this.selections = selections; }
    public void incrementSelections(int itemId) { selections.add(itemId); }
    public void removeFromSelections(int itemId) { selections.remove(itemId); }

    // Auto Selections Setters
    public void setAuto(Boolean auto) { this.auto = auto; }
    public void setMinVol(Integer minVol) { this.minVol = minVol; }
    public void setMaxBidAskVolRatio(Float maxBidAskVolRatio) { this.maxBidAskVolRatio = maxBidAskVolRatio; }
    public void setMinMargin(Integer minMargin) { this.minMargin = minMargin; }
    public void setMaxBidPrice(Integer maxBidPrice) { this.maxBidPrice = maxBidPrice; }
    public void setTradeRestricted(Boolean tradeRestricted) { this.tradeRestricted = tradeRestricted; }
    public void setMembers(Boolean members) { this.members = members; }
    public void setNumToSelect(Integer numToSelect) { this.numToSelect = numToSelect; }
}