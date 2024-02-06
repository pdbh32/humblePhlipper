package humblePhlipper.resources.data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Trades {
    public List<Trades.Trade> list;
    public Trades() {
        this.list = new ArrayList<>();
    }
    public Trades(String csv) {
        this.list = new ArrayList<>();
        String[] trades = csv.split("\\n");
        for (int i = 1; i < trades.length; i++) {
            Trades.Trade trade = new Trades.Trade(trades[i]);
            this.list.add(trade);
        }
    }
    public void increment(Trades trades) {
        for (Trades.Trade trade : trades.list) {
            this.increment(trade);
        }
    }
    public void increment(Trades.Trade trade) {
        this.list.add(trade);
    }
    public String getCSV() {
        String csv = "time,name,vol,price";
        for (Trades.Trade trade : this.list) {
            csv += "\n" + trade.getCSV();
        }
        return csv;
    }
    public Map<String, Trades> splitByName() {
        Map<String, Trades> tradesMap = new HashMap<>();
        for (Trade trade : this.list) {
            tradesMap.computeIfAbsent(trade.getName(), k -> new Trades()).increment(trade);
        }
        return tradesMap;
    }
    public Trades.Summary summarise() {
        Trades.Summary summary = new Trades.Summary();
        List<LocalDateTime> timeList = new ArrayList<>();
        for (Trades.Trade trade : this.list) {
            timeList.add(trade.getTime());
            summary.trades += 1;
            summary.vol += trade.getVol();
            summary.cumVol += trade.getVol() * (trade.getPrice() < 0 ? 1 : -1);
            summary.profit += trade.getVol() * trade.getPrice();
            summary.tax += trade.getVol() * (trade.getPrice() > 0 ? (int) (trade.getPrice()/99) : 0);
        }
        long runtimeMs = (!timeList.isEmpty()) ? Duration.between(Collections.min(timeList), Collections.max(timeList)).toMillis() : 0;
        summary.runtimeHours +=  (double) runtimeMs/3600000;
        return summary;
    }
    public String getError() {
        humblePhlipper.resources.data.Trades.Summary allSummary = summarise();
        if (allSummary.runtimeHours == 0) {
            return "<Error: No trades>";
        }
        Map<String, Trades> tradesMap = splitByName();
        for (Map.Entry<String, Trades> entry : tradesMap.entrySet()) {
            humblePhlipper.resources.data.Trades.Summary itemSummary = entry.getValue().summarise();
            if (itemSummary.cumVol != 0) {
                return "<Error: Cumulative volume of " + entry.getKey() + " != 0>";
            }
        }
        return null;
    }

    public static class Summary {
        public double profit;
        public double runtimeHours;
        public int trades;
        public int tax;
        public int cumVol;
        public int vol;

        public Summary() {
            profit = 0.0;
            runtimeHours = 0.0;
            trades = 0;
            tax = 0;
            cumVol = 0;
            vol = 0;
        }
    }

    public static class Trade {
        private final LocalDateTime time;
        private final String name;
        private final Integer vol;
        private final Double price;

        public Trade(LocalDateTime time, String name, Integer vol, Double price) {
            this.time = time;
            this.name = name;
            this.vol = vol;
            this.price = price;
        }
        public Trade(String CSV) {
            String[] values = CSV.split(",");
            this.time = LocalDateTime.parse(values[0]);
            this.name = values[1];
            this.vol = Integer.valueOf(values[2]);
            this.price = Double.valueOf(values[3]);
        }

        public String getCSV() {
            return time + "," + name + "," + vol + "," + price;
        }

        public LocalDateTime getTime() { return time; }

        public String getName() { return name; }

        public Integer getVol() { return vol; }

        public Double getPrice() { return price; }
    }

}
