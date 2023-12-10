//Item.java

import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;

import java.time.LocalDateTime;

public class Item {
    // Class fields
    public String name; // item name
    public int id; // item ID
    public int targetVol; // target volume, i.e., 4hr GE limit
    public double lastBuyPrice; // last buy price
    public double profit; // running profit calculated after each sale
    public int bought; // total bought
    public int sold; // total sold
    public int slot; // GE slot 0 to 7 or -1 (not trading)
    public String tradeHistory; // CSV of trades, `<time>,<name>,<quantity>,<price>`, where price is negative for bids and post-tax for asks
    public int bid; // latest bid/low/instasell price
    public int deltaBid; // bid_t - bid_{t-1}
    public int ask; // latest ask/high/instabuy price
    public int deltaAsk; // ask_t - ask_{t-1}

    // Constructor method to initialize field values
    public Item(String name, int id, int targetVol) {
        this.name = name;
        this.id = id;
        this.targetVol = targetVol;
        this.lastBuyPrice = -1;
        this.bought = 0;
        this.sold = 0;
        this.slot = -1;
        this.tradeHistory = "";
        this.bid = -1;
        this.deltaBid = -1;
        this.ask = -1;
        this.deltaAsk = -1;
    }

    // Update prices using OSRS wiki live price API
    public void updatePricesOSRSwiki() {
        this.deltaAsk = Main.api.latestMap.get(this.id).getHigh() - this.ask;
        this.ask = Main.api.latestMap.get(this.id).getHigh();
        this.deltaBid = Main.api.latestMap.get(this.id).getLow() - this.bid;
        this.bid = Main.api.latestMap.get(this.id).getLow();
    }

    // Alternatively, update prices using DreamBot's proxy
    public void updatePricesDreamBot() {
        this.deltaBid = LivePrices.getLow(this.id) - this.bid;
        this.bid = LivePrices.getLow(this.id);
        this.deltaAsk = LivePrices.getHigh(this.id) - this.ask;
        this.ask = LivePrices.getHigh(this.id);
    }

    // Check cancel conditions and make cancels
    public void checkCancel() {
        if (!GrandExchange.isOpen() || this.slot == -1) {
            return;
        }

        String status = Widgets.get(465, 7 + this.slot, 16).getText(); // Status on main 8-item interface, {'Buy', 'Sell', 'Empty'}
        int price = Integer.parseInt(Widgets.get(465, 7 + this.slot, 25).getText().replaceAll("[^0-9]", ""));
        int tradeWidth = Widgets.get(465, 7 + this.slot, 22).getWidth(); // Trade bar width on main 8-item interface, {0, 1, 2, ..., 104, 105}

        if ((status.equals("Buy") && (price != this.bid || 0.99 * this.ask - this.bid <= 0 || tradeWidth > 0 || !Main.bidding)) ||
             status.equals("Sell") && (price != this.ask)) {
            GrandExchange.cancelOffer(this.slot);
            Sleep.sleep(Main.SLEEP);
        }
    }

    public void collect() {
        if (!GrandExchange.isOpen() || this.slot == -1) {
            return;
        }
        int tradeWidth = Widgets.get(465, this.slot + 7, 22).getWidth(); // Trade bar width on main 8-item interface, {0, 1, 2, ..., 104, 105}
        if (tradeWidth != 105) {
            return;
        }

        // Open slot
        Widgets.get(465, this.slot + 7, 2).interact();
        Sleep.sleep(Main.SLEEP);

        // Get number of items and coins to be collected
        int itemsCollected = 0;
        double coinsCollected= 0; // coins have ID 995

        // Check first item slot
        if (GrandExchange.getOfferFirstItemWidget().getItemId() == this.id) {
            itemsCollected = GrandExchange.getOfferFirstItemWidget().getItemStack();
        } else if (GrandExchange.getOfferFirstItemWidget().getItemId() == 995) {
            coinsCollected = GrandExchange.getOfferFirstItemWidget().getItemStack();
        }

        // Check second item slot
        if (GrandExchange.getOfferSecondItemWidget().getItemId() == this.id && !GrandExchange.getOfferSecondItemWidget().isHidden()) {
            itemsCollected = GrandExchange.getOfferSecondItemWidget().getItemStack();
        } else if (GrandExchange.getOfferSecondItemWidget().getItemId() == 995 && !GrandExchange.getOfferSecondItemWidget().isHidden()) {
            coinsCollected = GrandExchange.getOfferSecondItemWidget().getItemStack();
        }

        // Deduce the number of items bought/sold and average unit prices for this trade
        int vol = 0;
        double price = 0;

        // - Price is a double because in OSRS it is possible for the latest bid-ask spread to go negative (e.g. no
        // current bid, buy order comes in above current ask, buy order treated as bid instead of market order, ask
        // offer executes as market order) which means our offers can be partially executed at different prices.
        // - Bid prices are negative.
        // - Ask prices are post-tax.

        if (Widgets.get(465, 15, 4).getText().equals("Buy offer")) {

            // Volume bought is items collected
            vol = itemsCollected;

            // Find the number of coins offered when making this bid offer
            String coinsOfferedString = Widgets.get(465,15,29).getText().replaceAll("[^0-9]", "");
            double coinsOffered = Double.parseDouble(coinsOfferedString);

            if (vol != 0) {
                price = -1.0 * (coinsOffered - coinsCollected) / vol;
                this.bought += vol;
                Logger.log(this.name + ", q: " + vol + ", p: " + price + ", total bought: " + this.bought + " = " + Math.floor(100 * this.bought / this.targetVol) + "%");
                this.lastBuyPrice = -1.0 * price;
            }

        } else if (Widgets.get(465, 15, 4).getText().equals("Sell offer")) {

            // Find the number of items offered when making this ask offer
            String itemsOfferedString = Widgets.get(465,15,18).getText().replaceAll("[^0-9]", "");
            int itemsOffered = Integer.parseInt(itemsOfferedString);

            // Quantity sold is items offered less items collected
            vol = itemsOffered - itemsCollected;

            if (vol != 0) {
                price = coinsCollected / vol;
                this.sold += vol;
                Logger.log(this.name + ", q: " + vol + ", p: " + price + ", total sold: " + this.sold + " = " + Math.floor(100 * this.sold / this.targetVol) + "%");
                this.profit += (price - this.lastBuyPrice) * vol;
            }
        }

        // Update trade history CSV and calculate change in netCoins
        if (vol != 0) {
            this.tradeHistory += "\n"+LocalDateTime.now()+","+this.name+","+vol+","+price;
        }

        // Click coins/items to collect them
        if (!Widgets.get(465, 24, 3).isHidden()) {
            Widgets.get(465, 24, 3).interact();
            Sleep.sleep(Main.SLEEP);
        }
        Widgets.get(465, 24, 2).interact();
        Sleep.sleep(Main.SLEEP);

        // Set trading as over
        this.slot = -1;
    }

    public void makeAsk() {
        if (!GrandExchange.isOpen() || this.slot != -1 || this.sold >= this.bought){
            return;
        }
        int slot = GrandExchange.getFirstOpenSlot();
        if (GrandExchange.sellItem(this.name, (this.bought - this.sold), this.ask)) {
            this.slot = slot;
            Sleep.sleep(Main.SLEEP);
        }
    }

    public void makeBid() {
        if (!GrandExchange.isOpen() || this.slot != -1 || this.bought >= this.targetVol || !Main.bidding || Inventory.count("Coins") < this.bid || 0.99 * this.ask - this.bid <= 0) {
            return;
        }
        int slot = GrandExchange.getFirstOpenSlot();
        int bidVol = (int) Math.min(Math.floor(Inventory.count("Coins")) / this.bid, (this.targetVol - this.bought)); // Math.min{<maximum we can afford>, <remaining GE buy limit>}
        bidVol = (int) Math.min(bidVol, Math.floor((double) (Main.config.maxBidVol * this.targetVol) /100));
        if (GrandExchange.buyItem(this.name, bidVol, this.bid)) {
            this.slot = slot;
            Sleep.sleep(Main.SLEEP);
        }
    }
}