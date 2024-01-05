// Resource\SavedData\Trade.java

package humblePhlipper.Resources.SavedData;

import java.time.LocalDateTime;

public class Trade {
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
