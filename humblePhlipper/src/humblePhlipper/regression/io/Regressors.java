package humblePhlipper.regression.io;

public class Regressors {
    private Regressors() {
    }
    public static String[] getArray() {
        return new String[]{"intercept", // Model k = 0
                "runtime",
                "runtime%4", // Model k = 2
                "members",
                "tradeRestricted", // Model k = 4
                "fiveMinute",
                "oneHour",
                "bestOfLatestFiveMinute",
                "worstOfLatestFiveMinute",
                "pricingOffset",
                "pricingOffset^2", // Model k = 10
                "minVol",
                "minVol^2",
                "minMargin",
                "minMargin^2",
                "maxBidVolAskRatio",
                "maxBidVolAskRatio^2",
                "maxBidPrice",
                "maxBidPrice^2", // Model k = 18
                "maxBidValue",
                "maxBidValue^2",
                "maxBidVol",
                "maxBidVol^2"}; // Model k = 22
    }

    public static String getCSV() {
        String[] regressors = getArray();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < regressors.length; i++) {
            stringBuilder.append("\"").append(regressors[i]).append("\"");

            if (i < regressors.length - 1) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }
}
