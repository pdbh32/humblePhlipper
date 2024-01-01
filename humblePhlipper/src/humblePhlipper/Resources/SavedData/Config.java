// Resources\SavedData\Config.java

package humblePhlipper.Resources.SavedData;

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
    @SerializedName("profitCutOff")
    @Expose
    private Integer profitCutOff;
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
    @SerializedName("mingMargin")
    @Expose
    private Integer minMargin;
    @SerializedName("maxBidPrice")
    @Expose
    private Integer maxBidPrice;
    @SerializedName("tradeRestricted")
    @Expose
    private Boolean tradeRestricted;
    @SerializedName("numToSelect")
    @Expose
    private Integer numToSelect;

    public Config() {
        // Miscellaneous
        this.timeout = 240F;
        this.profitCutOff = Integer.MAX_VALUE;
        this.sysExit = false;
        this.maxBidValue = 1000000;
        this.maxBidVol = 10000;

        // Bid Priority
        this.priorityProfit = 100;
        this.priorityVol = 0;
        this.priorityCapitalBinding = 0;

        // Pricing
        this.pricing = "latest";

        // Selections
        this.selections = new LinkedHashSet<>();
        this.selections.add(1511); // Logs
        this.selections.add(560); // Death rune

        // Auto Selections
        this.auto = true;
        this.minVol = 100000;
        this.minMargin = -10;
        this.maxBidPrice = 20000;
        this.maxBidAskVolRatio = 3.0f;
        this.tradeRestricted = false;
        this.numToSelect = 64;
    }

    // Miscellaneous Getters
    public Float getTimeout() {
        return timeout;
    }
    public Integer getProfitCutOff() { return profitCutOff; }
    public Boolean getSysExit() {
        return sysExit;
    }
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

    public Integer getNumToSelect() { return numToSelect; }

    // Miscellaneous Setters
    public void setTimeout(Float timeout) { this.timeout = timeout; }
    public void setProfitCutOff(Integer profitCutoff) { this.profitCutOff = profitCutoff; }
    public void setSysExit(Boolean sysExit) { this.sysExit = sysExit; }
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

    // Selections Setters
    public void setSelections(Set<Integer> selections) { this.selections = selections; }
    public void incrementSelections(int itemId) { selections.add(itemId); }

    // Auto Selections Setters
    public void setAuto(Boolean auto) { this.auto = auto; }
    public void setMinVol(Integer minVol) { this.minVol = minVol; }
    public void setMaxBidAskVolRatio(Float maxBidAskVolRatio) { this.maxBidAskVolRatio = maxBidAskVolRatio; }
    public void setMinMargin(Integer minMargin) { this.minMargin = minMargin; }
    public void setMaxBidPrice(Integer maxBidPrice) { this.maxBidPrice = maxBidPrice; }
    public void setTradeRestricted(Boolean tradeRestricted) { this.tradeRestricted = tradeRestricted; }
    public void setNumToSelect(Integer numToSelect) { this.numToSelect = numToSelect; }
}
