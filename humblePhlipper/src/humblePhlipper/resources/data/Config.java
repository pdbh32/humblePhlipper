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
    @SerializedName("autoBond")
    @Expose
    private Boolean autoBond;
    @SerializedName("sysExit")
    @Expose
    private Boolean sysExit;
    @SerializedName("discordWebhook")
    @Expose
    private String discordWebhook;
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
    @SerializedName("noSelfCompeting")
    @Expose
    private Boolean noSelfCompeting;
    @SerializedName("cancelPartialBids")
    @Expose
    private Boolean cancelPartialBids;
    @SerializedName("neverSellAtLoss")
    @Expose
    private Boolean neverSellAtLoss;

    // Bid Priority
    @SerializedName("priorityProfit")
    @Expose
    private Integer priorityProfit;
    @SerializedName("priorityVol")
    @Expose
    private Integer priorityVol;

    // Pricing
    @SerializedName("pricing")
    @Expose
    private String pricing;
    @SerializedName("pricingOffset")
    @Expose
    private Integer pricingOffset;
    @SerializedName("pricingOffsetAsPercentage")
    @Expose
    private Boolean pricingOffsetAsPercentage;

    // Selections
    @SerializedName("selections")
    @Expose
    private Set<Integer> selections;
    @SerializedName("omissions")
    @Expose
    private Set<Integer> omissions;

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
    @SerializedName("minBidPrice")
    @Expose
    private Integer minBidPrice;
    @SerializedName("tradeRestricted")
    @Expose
    private Boolean tradeRestricted;
    @SerializedName("members")
    @Expose
    private Boolean members;

    public Config() {
        // Miscellaneous
        this.timeout = 240F;
        this.autoBond = false;
        this.sysExit = false;
        this.discordWebhook = null;
        this.debug = false;

        // Bid Restrictions
        this.maxBidValue = 1000000;
        this.maxBidVol = 20000;
        this.noSelfCompeting = false;
        this.cancelPartialBids = true;
        this.neverSellAtLoss = false;

        // Bid Priority
        this.priorityProfit = 100;
        this.priorityVol = 100;

        // Pricing
        this.pricing = "bestOfLatestFiveMinute";
        this.pricingOffset = 0;
        this.pricingOffsetAsPercentage = false;

        // Selections
        this.selections = new LinkedHashSet<>();
        this.selections.add(1511); // Logs
        this.selections.add(560); // Death rune
        this.omissions = new LinkedHashSet<>();
        this.omissions.add(13190); // Bond

        // Auto Selections
        this.auto = true;
        this.minVol = 10000;
        this.maxBidAskVolRatio = 3.0f;
        this.minMargin = 2;
        this.maxBidPrice = 20000;
        this.minBidPrice = 0;
        this.tradeRestricted = false;
        this.members = false;
    }

    // Miscellaneous Getters
    public Float getTimeout() {
        return timeout;
    }
    public Boolean getAutoBond() { return autoBond; }
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
    public Boolean getNoSelfCompeting() {
        return noSelfCompeting;
    }
    public Boolean getCancelPartialBids() {
        return cancelPartialBids;
    }
    public Boolean getNeverSellAtLoss() {
        return neverSellAtLoss;
    }

    // Bid Priority Getters
    public Integer getPriorityProfit() {
        return priorityProfit;
    }
    public Integer getPriorityVol() {
        return priorityVol;
    }

    // Pricing Getters
    public String getPricing() {
        return pricing;
    }
    public Integer getPricingOffset() {
        return pricingOffset;
    }
    public Boolean getPricingOffsetAsPercentage() { return pricingOffsetAsPercentage; }

    // Selections Getters
    public Set<Integer> getSelections() {
        return selections;
    }
    public Set<Integer> getOmissions() {
        return omissions;
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
    public Integer getMinBidPrice() { return minBidPrice; }

    public Boolean getTradeRestricted() { return tradeRestricted; }

    public Boolean getMembers() { return members; }

    // Miscellaneous Setters
    public void setTimeout(Float timeout) { this.timeout = timeout; }
    public void setAutoBond(Boolean autoBond) { this.autoBond = autoBond; }
    public void setSysExit(Boolean sysExit) { this.sysExit = sysExit; }
    public void setDiscordWebhook(String discordWebhook ) { this.discordWebhook = discordWebhook; }
    public void setDebug(Boolean debug) { this.debug = debug; }

    // Bid Restrictions Setters
    public void setMaxBidValue(Integer maxBidValue) { this.maxBidValue = maxBidValue; }
    public void setMaxBidVol(Integer maxBidVol) { this.maxBidVol = maxBidVol; }
    public void setNoSelfCompeting(Boolean noSelfCompeting) { this.noSelfCompeting = noSelfCompeting; }
    public void setCancelPartialBids(Boolean cancelPartialBids) { this.cancelPartialBids = cancelPartialBids; }
    public void setNeverSellAtLoss(Boolean neverSellAtLoss) { this.neverSellAtLoss = neverSellAtLoss; }

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

    // Pricing Setters
    public void setPricing(String pricing) {
        this.pricing = pricing;
    }
    public void setPricingOffset(Integer pricingOffset) {
        this.pricingOffset = pricingOffset;
    }
    public void setPricingOffsetAsPercentage(Boolean pricingOffsetAsPercentage) { this.pricingOffsetAsPercentage = pricingOffsetAsPercentage; }

    // Selections Setters
    public void setSelections(Set<Integer> selections) { this.selections = selections; }
    public void incrementSelections(int itemId) { selections.add(itemId); }
    public void removeFromSelections(int itemId) { selections.remove(itemId); }
    public void setOmissions(Set<Integer> omissions) { this.omissions = omissions; }
    public void incrementOmissions(int itemId) { omissions.add(itemId); }
    public void removeFromOmissions(int itemId) { omissions.remove(itemId); }

    // Auto Selections Setters
    public void setAuto(Boolean auto) { this.auto = auto; }
    public void setMinVol(Integer minVol) { this.minVol = minVol; }
    public void setMaxBidAskVolRatio(Float maxBidAskVolRatio) { this.maxBidAskVolRatio = maxBidAskVolRatio; }
    public void setMinMargin(Integer minMargin) { this.minMargin = minMargin; }
    public void setMaxBidPrice(Integer maxBidPrice) { this.maxBidPrice = maxBidPrice; }
    public void setMinBidPrice(Integer minBidPrice) { this.minBidPrice = minBidPrice; }
    public void setTradeRestricted(Boolean tradeRestricted) { this.tradeRestricted = tradeRestricted; }
    public void setMembers(Boolean members) { this.members = members; }
}