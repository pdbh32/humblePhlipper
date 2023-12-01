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
    // Constant
    int SLEEP = 600; // in milliseconds

    // Class fields
    String name; // item name
    int id; // item ID
    int targetVol; // target volume, i.e., 4hr GE limit
    int bidVol; // our current bid-offer amount
    int bought; // total bought
    int sold; // total sold
    int slot; // GE slot 0 to 7 or -1 (not trading)
    String tradeHistory; // CSV of trades, `<time>,<quantity>,<price>`, where price is negative for bids
    int bid; // latest bid/low/instasell price
    int deltaBid; // bid_t - bid_{t-1}
    int ask; // latest ask/high/instabuy price
    int deltaAsk; // ask_t - ask_{t-1}

    // Constructor method to initialize field values
    public Item(String name, int id, int targetVol) {
        this.name = name;
        this.id = id;
        this.targetVol = targetVol;
        this.bidVol = 0;
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
    public void checkCancel() {
        if (this.slot != -1) { // Trading
            if (Widgets.get(465, 7 + this.slot, 16).getText().equals("Buy")) { // Bidding

                // If bid price has changed or spread is now unprofitable or progress has been made, cancel
                if (this.deltaBid != 0 || 0.99 * this.ask - this.bid <= 0 || Widgets.get(465, this.slot + 7, 22).getWidth() > 0) {
                    GrandExchange.cancelOffer(this.slot);
                    Sleep.sleep(SLEEP);
                }
            } else { // Asking

                // If ask price has changed, cancel
                if (this.deltaAsk != 0) {  // || Widgets.get(465, this.slot + 7, 22).getWidth() > 0
                    GrandExchange.cancelOffer(this.slot);
                    Sleep.sleep(SLEEP);
                }
            }
        }
    }

    public void collect() {
        if (this.slot != -1) { // trading
            if (Widgets.get(465, this.slot + 7, 22).getWidth() == 105) { // done-trading (progress bar width is 105/105)

                // Open slot
                Widgets.get(465, this.slot + 7, 2).interact();
                Sleep.sleep(SLEEP);

                // Get number of items and coins to be collected
                int itemsCollected = 0;
                int coinsCollected= 0;

                // 995 is the Item ID of Coins

                // Check first item slot
                if (GrandExchange.getOfferFirstItemWidget().getItemId() == this.id && !GrandExchange.getOfferFirstItemWidget().isHidden()) {
                    itemsCollected = GrandExchange.getOfferFirstItemWidget().getItemStack();
                } else if (GrandExchange.getOfferFirstItemWidget().getItemId() == 995 && !GrandExchange.getOfferFirstItemWidget().isHidden()) {
                    coinsCollected = GrandExchange.getOfferFirstItemWidget().getItemStack();
                }

                // Check second item slot
                if (GrandExchange.getOfferSecondItemWidget().getItemId() == this.id && !GrandExchange.getOfferSecondItemWidget().isHidden()) {
                    itemsCollected = GrandExchange.getOfferSecondItemWidget().getItemStack();
                } else if (GrandExchange.getOfferSecondItemWidget().getItemId() == 995 && !GrandExchange.getOfferSecondItemWidget().isHidden()) {
                    coinsCollected = GrandExchange.getOfferSecondItemWidget().getItemStack();
                }

                // Deduce the number of items bought/sold and unit prices

                if (Widgets.get(465, 15, 4).getText().equals("Buy offer")) { // If bidding
                    int justBought = itemsCollected;
                    if (justBought != 0) {
                        int boughtAt =  (this.bidVol * (this.bid - this.deltaBid) - coinsCollected) / itemsCollected;
                        this.bought += justBought;
                        this.tradeHistory += "\n" + LocalDateTime.now() + "," + justBought + "," + "-" + boughtAt;
                        Logger.log(this.name + ", q: " + justBought + ", p: " + "-" + boughtAt + ", total bought: " + Math.floor(100 * this.bought / this.targetVol) + "%");
                    }

                } else { // Else if asking
                    int justSold = (this.bought - this.sold) - itemsCollected;
                    if (justSold != 0) {
                        int soldAt = (int) Math.floor(coinsCollected/(justSold*0.99));
                        this.sold += justSold;
                        this.tradeHistory += "\n" + LocalDateTime.now() + "," + justSold + "," + soldAt; // or (this.ask - this.deltaAsk)
                        Logger.log(this.name + ", q: " + justSold + ", p: " + soldAt + ", total sold: " + Math.floor(100 * this.sold / this.targetVol) + "%");
                    }
                }

                // Set trading as over
                this.slot = -1;

                // Click coins/items to collect them
                if (Widgets.get(465, 24, 3).isHidden()) {
                    Widgets.get(465, 24, 2).interact();
                } else {
                    Widgets.get(465, 24, 2).interact();
                    Sleep.sleep(SLEEP);
                    Widgets.get(465, 24, 3).interact();
                }
                Sleep.sleep(SLEEP);
            }
        }
    }

    public void makeAsk() {
        if (this.slot == -1) {
            int slot = GrandExchange.getFirstOpenSlot();
            if (GrandExchange.sellItem(this.name, Inventory.count(this.name), this.ask)) {
                this.slot = slot;
                Sleep.sleep(SLEEP);
            }
        }
    }
    public void makeBid() {
        if (this.slot == -1 && this.bought < this.targetVol && 0.99 * this.ask - this.bid > 0) {
            int slot = GrandExchange.getFirstOpenSlot();

            // New bid volume is `Math.min{<maximum we can afford>, <remaining GE buy limit>}`
            this.bidVol = (int) Math.min(Math.floor(Inventory.count("Coins")) / this.bid, (this.targetVol - this.bought));
            if (this.bidVol > 0) {
                if (GrandExchange.buyItem(this.name, bidVol, this.bid)) {
                    this.slot = slot;
                    Sleep.sleep(SLEEP);
                }
            }
        }
    }
}