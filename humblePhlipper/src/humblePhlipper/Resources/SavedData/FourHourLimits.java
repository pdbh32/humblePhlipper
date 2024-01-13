// Resources\SavedData\FourHourLimits.java

package humblePhlipper.resources.savedData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.HashMap;

public class FourHourLimits extends HashMap<Integer, FourHourLimits.FourHourLimit> {

    public static class FourHourLimit {

        @SerializedName("refreshTime")
        @Expose
        private LocalDateTime refreshTime;

        @SerializedName("usedLimit")
        @Expose
        private Integer usedLimit;

        public FourHourLimit() {
            this.usedLimit = 0;
            this.refreshTime = LocalDateTime.now();
        }

        public LocalDateTime getRefreshTime() {
            return refreshTime;
        }

        public void setRefreshTime(LocalDateTime refreshTime) {
            this.refreshTime = refreshTime;
        }

        public Integer getUsedLimit() {
            return usedLimit;
        }

        public void setUsedLimit(Integer usedLimit) {
            this.usedLimit = usedLimit;
        }

        public void incrementUsedLimit(Integer vol) { this.usedLimit += vol; }
    }
}