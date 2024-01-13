package humblePhlipper.regression.io;

import Jama.Matrix;

import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.Logger;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class DM {
    private int k;
    public int cumRuntimeHours;
    public int cumProfit;
    public Jama.Matrix Y;
    public Jama.Matrix X;
    public DM(int k) {
        this.k = k;
        this.cumRuntimeHours = 0;
        this.cumProfit = 0;
        designMatrices();
    }

    private static Map<String, Double> parseCSV(String tradesCSV, String fileName) {
        Map<String, Double> dataMap = new HashMap<>();
        dataMap.put("profit", 0.0);
        dataMap.put("runtime", null);

        List<LocalDateTime> timeList = new ArrayList<>();
        Map<String, Integer> itemCumVolMap = new HashMap<>();

        String[] trades = tradesCSV.split("\\n");
        for (int i = 1; i < trades.length; i++) {
            humblePhlipper.resources.savedData.Trade trade = new humblePhlipper.resources.savedData.Trade(trades[i]);

            if (trade.getPrice() == 0) {
                Logger.log("<Error: Price = 0>" + fileName + "</Error>");
                return null;
            }

            dataMap.merge("profit", trade.getPrice() * trade.getVol(), Double::sum);
            timeList.add(trade.getTime());
            itemCumVolMap.merge(
                    trade.getName(),
                    trade.getVol() * (trade.getPrice() < 0 ? 1 : -1),
                    Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : itemCumVolMap.entrySet()) {
            if (entry.getValue() != 0) {
                Logger.log("<Error: Cumulative Volume of " + entry.getKey() + " != 0>" + fileName + "</Error>");
                return null;
            }
        }

        long runtimeMs = (!timeList.isEmpty()) ? Duration.between(Collections.min(timeList), Collections.max(timeList)).toMillis() : 0;
        if (runtimeMs == 0.0) {
            Logger.log("<Error: No Trades>" + fileName + "</Error>");
            return null;
        }

        dataMap.put("runtime", (double) runtimeMs/3600000);
        return dataMap;
    }

    private static double[] y(double profit, double runtime) {
        double profitPerHour = profit/runtime;
        double[] y = {profitPerHour};
        return y;
    }

    private static double[] x(humblePhlipper.resources.savedData.Config config, double runtime, int k) {
        double[] x = {
                1.0,  // intercept

                runtime,
                runtime % 4,

                config.getMembers() ? 1.0 : 0.0,
                config.getTradeRestricted() ? 1.0 : 0.0,

                "fiveMinute".equals(config.getPricing()) ? 1.0 : 0.0,
                "oneHour".equals(config.getPricing()) ? 1.0 : 0.0,
                "bestOfLatestFiveMinute".equals(config.getPricing()) ? 1.0 : 0.0,
                "worstOfLatestFiveMinute".equals(config.getPricing()) ? 1.0 : 0.0,
                (double) config.getPricingOffset(),
                (double) config.getPricingOffset() * config.getPricingOffset(),

                (double) config.getMinVol(),
                (double) config.getMinVol() * config.getMinVol(),
                (double) config.getMinMargin(),
                (double) config.getMinMargin() * config.getMinMargin(),
                (double) config.getMaxBidAskVolRatio(),
                (double) config.getMaxBidAskVolRatio() * config.getMaxBidAskVolRatio(),
                (double) config.getMaxBidPrice(),
                (double) config.getMaxBidPrice() * config.getMaxBidPrice(),

                (double) config.getMaxBidValue(),
                (double) config.getMaxBidValue() * config.getMaxBidValue(),
                (double) config.getMaxBidVol(),
                (double) config.getMaxBidVol() * config.getMaxBidVol(),
        };
        return Arrays.stream(x).limit(k + 1).toArray();
    }

    private void designMatrices() {
        String historyPath = System.getProperty("scripts.path") + File.separator + "humblePhlipper" + File.separator + "History";
        File historyDirectory = new File(historyPath);
        File[] files = historyDirectory.listFiles();

        if (files == null) {
            return;
        }

        List<double[]> yList = new ArrayList<>();
        List<double[]> xList = new ArrayList<>();

        for (File file : files) {
            Map sessionHistory = ScriptSettings.load(Map.class, "humblePhlipper", "History", file.getName());
            String tradesCSV = (String) sessionHistory.get("tradesCSV");
            String configJSON = (String) sessionHistory.get("configJSON");
            humblePhlipper.resources.savedData.Config config = humblePhlipper.Main.rm.gson.fromJson(configJSON, humblePhlipper.resources.savedData.Config.class);

            Map<String, Double> dataMap = parseCSV(tradesCSV, file.getName());
            if (dataMap == null) {
                continue;
            }

            cumProfit += dataMap.get("profit");
            cumRuntimeHours += dataMap.get("runtime");

            double[] y = y(dataMap.get("profit"), dataMap.get("runtime"));
            yList.add(y);

            double[] x = x(config, dataMap.get("runtime"), k);
            xList.add(x);
        }

        Y = new Matrix(yList.stream().toArray(double[][]::new));
        X = new Matrix(xList.stream().toArray(double[][]::new));
    }
}
