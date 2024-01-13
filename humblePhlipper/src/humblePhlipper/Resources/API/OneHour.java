// Resources\API\OneHour.java

package humblePhlipper.resources.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OneHour {

    @SerializedName("avgHighPrice")
    @Expose
    private Integer avgHighPrice;

    @SerializedName("highPriceVolume")
    @Expose
    private Integer highPriceVolume;

    @SerializedName("avgLowPrice")
    @Expose
    private Integer avgLowPrice;

    @SerializedName("lowPriceVolume")
    @Expose
    private Integer lowPriceVolume;

    public OneHour() {
        this.avgHighPrice = null;
        this.highPriceVolume = 0;
        this.avgLowPrice = null;
        this.lowPriceVolume = 0;
    }

    public Integer getAvgHighPrice() { return avgHighPrice; }
    public void setAvgHighPrice(Integer avgHighPrice) {
        this.avgHighPrice = avgHighPrice;
    }
    public Integer getHighPriceVolume() {
        return highPriceVolume;
    }
    public void setHighPriceVolume(Integer highPriceVolume) { this.highPriceVolume = highPriceVolume; }
    public Integer getAvgLowPrice() {
        return avgLowPrice;
    }
    public void setAvgLowPrice(Integer avgLowPrice) {
        this.avgLowPrice = avgLowPrice;
    }
    public Integer getLowPriceVolume() { return lowPriceVolume; }
    public void setLowPriceVolume(Integer lowPriceVolume) { this.lowPriceVolume = lowPriceVolume; }
}
