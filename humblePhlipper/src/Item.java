//Item.java

import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class Item {
    // Class fields
    String name; // item name
    int id; // item ID
    int targetVol; // target volume, i.e., 4hr GE limit
    int bought; // total bought
    int sold; // total sold
    int slot; // GE slot 0 to 7 or -1 (not trading)
    String tradeHistory; // CSV of trades, `<time>,<name>,<quantity>,<price>`, where price is negative for bids and post-tax for asks
    int bid; // latest bid/low/instasell price
    int deltaBid; // bid_t - bid_{t-1}
    int ask; // latest ask/high/instabuy price
    int deltaAsk; // ask_t - ask_{t-1}

    // Constructor method to initialize field values
    public Item(String name, int id, int targetVol) {
        this.name = name;
        this.id = id;
        this.targetVol = targetVol;
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
        try {
            // Set the API
            String url = "https://prices.runescape.wiki/api/v1/osrs/latest?id=" + this.id;
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // Set the request method
            connection.setRequestMethod("GET");

            // Set the header
            connection.setRequestProperty("User-Agent", "f2p_flipper");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response from the API
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response as a string
                String jsonString = response.toString();

                // `jsonString` looks like:
                // {"data":{"453":{"high":177,"highTime":1701439802,"low":175,"lowTime":1701439824}}}

                // Extract content within innermost braces
                int startIdx = jsonString.lastIndexOf("{") + 1;
                int endIdx = jsonString.indexOf("}", startIdx);
                String innerContent = jsonString.substring(startIdx, endIdx);

                // `innerContent` looks like:
                // "high":177,"highTime":1701439802,"low":175,"lowTime":1701439824

                // Split the inner content, extract key-value pairs, and update prices
                String[] parts = innerContent.split(",");
                for (String part : parts) {
                    String[] keyValue = part.split(":");
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    switch (key) {
                        case "\"high\"":
                            this.deltaAsk = Integer.parseInt(value) - this.ask;
                            this.ask = Integer.parseInt(value);
                            break;
                        case "\"low\"":
                            this.deltaBid = Integer.parseInt(value) - this.bid;
                            this.bid = Integer.parseInt(value);
                            break;
                    }
                }
            } else {
                Logger.log("Error: HTTP request failed with response code " + responseCode);
            }
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    // Alternatively, update prices using DreamBot's proxy
    public void updatePricesDreamBot() {
        this.deltaBid = LivePrices.getLow(this.id) - this.bid;
        this.bid = LivePrices.getLow(this.id);
        this.deltaAsk = LivePrices.getHigh(this.id) - this.ask;
        this.ask = LivePrices.getHigh(this.id);
    }

    // Check cancel conditions and make cancels
    public void checkCancel(Boolean stopBidding) {
        if (!GrandExchange.isOpen() || this.slot == -1) {
            return;
        }

        String status = Widgets.get(465, 7 + this.slot, 16).getText(); // Status on main 8-item interface, {'Buy', 'Sell', 'Empty'}
        int tradeWidth = Widgets.get(465, this.slot + 7, 22).getWidth(); // Trade bar width on main 8-item interface, {0, 1, 2, ..., 104, 105}

        if ((status.equals("Buy") && (this.deltaBid != 0 || 0.99 * this.ask - this.bid <= 0 || tradeWidth > 0 || stopBidding)) ||
             status.equals("Sell") && (this.deltaAsk != 0)) {
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
            }
        }

        // Update trade history CSV
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

    public void makeBid(Boolean stopBidding) {
        if (!GrandExchange.isOpen() || this.slot != -1 || this.bought >= this.targetVol || stopBidding || Inventory.count("Coins") < this.bid || 0.99 * this.ask - this.bid <= 0) {
            return;
        }
        int slot = GrandExchange.getFirstOpenSlot();
        int bidVol = (int) Math.min(Math.floor(Inventory.count("Coins")) / this.bid, (this.targetVol - this.bought)); // Math.min{<maximum we can afford>, <remaining GE buy limit>}
        if (GrandExchange.buyItem(this.name, bidVol, this.bid)) {
            this.slot = slot;
            Sleep.sleep(Main.SLEEP);
        }
    }
}