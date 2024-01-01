// Resources\API\Latest.java

package humblePhlipper.Resources.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Latest {

    @SerializedName("high")
    @Expose
    private Integer high;
    @SerializedName("highTime")
    @Expose
    private Long highTime;
    @SerializedName("low")
    @Expose
    private Integer low;
    @SerializedName("lowTime")
    @Expose
    private Long lowTime;

    public Latest() {
        this.high = null;
        this.highTime = null;
        this.low = null;
        this.lowTime = null;
    }

    public Integer getHigh() {return high;}

    public void setHigh(Integer high) {
        this.high = high;
    }

    public Long getHighTime() {
        return highTime;
    }

    public void setHighTime(Long highTime) {
        this.highTime = highTime;
    }

    public Integer getLow() {
        return low;
    }

    public void setLow(Integer low) {
        this.low = low;
    }

    public Long getLowTime() {
        return lowTime;
    }

    public void setLowTime(Long lowTime) {
        this.lowTime = lowTime;
    }
}
