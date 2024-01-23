package humblePhlipper.regression.io;

import Jama.Matrix;

import org.dreambot.api.settings.ScriptSettings;

import java.io.File;
import java.util.*;

public class DM {
    private File[] files;
    private int k;
    public Jama.Matrix Y; // Regressand
    public Jama.Matrix X; // Regressor
    public Jama.Matrix Weights; // n x 1 column vector of runtimeHours
    public double cumRuntimeHours = 0.0;
    public double cumProfit = 0.0;
    public int cumTrades = 0;
    public int cumTax = 0;
    public String errors = "";
    public DM(File[] files, int k) {
        this.files = files;
        this.k = k;
        designMatrices();
    }

    public DM(File file, int k) {
        this.files = new File[]{file};
        this.k = k;
        designMatrices();
    }

    private static double[] y(double profit, double runtimeHours) {
        double profitPerHour = profit/runtimeHours;
        double[] y = {profitPerHour};
        return y;
    }

    private static double[] x(humblePhlipper.resources.savedData.Config config, double runtimeHours, int k) {
        double[] x = {
                1.0,  // intercept

                runtimeHours,
                runtimeHours % 4,

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

    private static double[] weight(double runtimeHours) {
        double[] weight = {runtimeHours};
        return weight;
    }

    private void designMatrices() {
        List<double[]> yList = new ArrayList<>();
        List<double[]> xList = new ArrayList<>();
        List<double[]> weightList = new ArrayList<>();

        for (File file : files) {
            Map sessionHistory = ScriptSettings.load(Map.class, "humblePhlipper", "History", file.getName());

            String tradesCSV = (String) sessionHistory.get("tradesCSV");
            humblePhlipper.resources.savedData.Trades trades = new humblePhlipper.resources.savedData.Trades(tradesCSV);

            String configJSON = (String) sessionHistory.get("configJSON");
            humblePhlipper.resources.savedData.Config config = humblePhlipper.Main.rm.gson.fromJson(configJSON, humblePhlipper.resources.savedData.Config.class);

            String error = trades.getError();
            if (error != null) {
                errors += error + file.getName() + "</Error>\n";
                continue;
            }

            humblePhlipper.resources.savedData.Trades.Summary summary = trades.summarise();

            cumProfit += summary.profit;
            cumRuntimeHours += summary.runtimeHours;
            cumTax += summary.tax;
            cumTrades += summary.trades;

            double[] y = y(summary.profit, summary.runtimeHours);
            yList.add(y);

            double[] x = x(config, summary.runtimeHours, k);
            xList.add(x);

            double[] weight = weight(summary.runtimeHours);
            weightList.add(weight);
        }

        Y = new Matrix(yList.stream().toArray(double[][]::new));
        X = new Matrix(xList.stream().toArray(double[][]::new));
        Weights = new Matrix(weightList.stream().toArray(double[][]::new));

        if (errors.length() > 0) {
            errors = errors.substring(0, errors.length() - 1); // Remove trailing \n
        }
    }
}
