// Resource\SavedData\History.java

package humblePhlipper.Resources.SavedData;

import java.time.LocalDateTime;

public class History {
    private final LocalDateTime time;
    private final String name;
    private final Integer vol;
    private final Double price;
    public History(LocalDateTime time, String name, Integer vol, Double price) {
        this.time = time;
        this.name = name;
        this.vol = vol;
        this.price = price;
    }
    public History(String time, String name, String vol, String price) {
        this.time = LocalDateTime.parse(time);
        this.name = name;
        this.vol = Integer.valueOf(vol);
        this.price = Double.valueOf(price);
    }
    public String getCSV() {
        return time + "," + name + "," + vol + "," + price;
    }
}
