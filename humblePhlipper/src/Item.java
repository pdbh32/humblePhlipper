//Item.java
public class Item {
    // Class fields
    public int id; // item ID
    public int targetVol; // target volume, i.e., 4hr GE limit

    public String name; // item name
    public double lastBuyPrice; // last buy price
    public double profit; // running profit calculated after each sale
    public int bought; // total bought
    public int sold; // total sold
    public String tradeHistory; // CSV of trades, `<time>,<name>,<quantity>,<price>`, where price is negative for bids and post-tax for asks

    // Constructor method to initialize field values
    public Item(int id, int targetVol) {
        this.id = id;
        this.targetVol = targetVol;
        this.name = Main.api.mappingMap.get(id).getName();
        this.lastBuyPrice = -1;
        this.bought = 0;
        this.sold = 0;
        this.tradeHistory = "";
    }
}