package humblePhlipper.resources.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class FourHourLimits extends HashMap<Integer, FourHourLimits.FourHourLimit> {

    public static class FourHourLimit {

        @SerializedName("lastReset")
        @Expose
        private long lastReset; // epochMilli

        @SerializedName("usedLimit")
        @Expose
        private int usedLimit;

        public FourHourLimit() {
            this.lastReset = 0;
            this.usedLimit = 0;
        }

        public long getRefreshTime() {
            return lastReset;
        }

        public void setRefreshTime(long refreshTime) {
            this.lastReset = refreshTime;
        }

        public int getUsedLimit() {
            return usedLimit;
        }

        public void setUsedLimit(Integer usedLimit) {
            this.usedLimit = usedLimit;
        }

        public void incrementUsedLimit(Integer vol) { this.usedLimit += vol; }
        public double getCountdownMinutes() {
            return 240 - Duration.between(Instant.ofEpochMilli(lastReset), Instant.now()).toMinutes();
        }
    }
}