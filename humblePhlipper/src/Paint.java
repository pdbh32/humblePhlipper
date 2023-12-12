//Paint.java

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Paint {
    private static final Color recBg = new Color(0, 0, 0, 127);
    public void onPaint(Graphics g) {
        // Create an itemList of the Item values in itemMap and sort alphabetically
        List<Item> itemList = new ArrayList<>(Main.config.itemMap.values());
        Collections.sort(itemList, Comparator.comparing(item -> item.name));

        // Define table row count
        int tableRowCount = Math.min((320 / 20), 1 + itemList.size());

        // Draw background rectangle for table (1)
        g.setColor(recBg);
        g.fillRect(7,7,757, tableRowCount *20);

        int x = 15;
        int y = 20;
        double profit = 0;

        // Draw table headers
        g.setColor(Color.white);
        g.drawString("Item Name", x, y);
        g.drawString("Profit", x + 150, y);
        g.drawString("Bought", x + 250, y);
        g.drawString("Sold", x + 350, y);
        g.drawString("Target", x + 450, y);
        g.drawString("Bid", x + 550, y);
        g.drawString("Ask", x + 650, y);
        y += 20;

        for (Item item : itemList) {
            if (y > 20 + 15*20) {
                break;
            }
            // Draw table rows
            g.drawString(item.name, x, y);
            g.drawString(String.valueOf(Math.round(item.profit)), x + 150, y);
            g.drawString(String.valueOf(item.bought), x + 250, y);
            g.drawString(String.valueOf(item.sold), x + 350, y);
            g.drawString(String.valueOf(item.targetVol), x + 450, y);
            g.drawString(String.valueOf(Main.api.latestMap.get(item.id).getLow()), x + 550, y);
            g.drawString(String.valueOf(Main.api.latestMap.get(item.id).getHigh()), x + 650, y);

            // Update total profit
            profit += item.profit;
            y += 20;
        }

        g.setColor(recBg);
        g.fillRect(7,344,506,132);
        g.setColor(Color.WHITE);
        g.drawString("Profit: " + Math.round(profit), 13, 360);
        g.drawString("Runtime: " + Main.timer.formatTime(), 13, 380);
        g.drawString("Timeout (Minutes): " + Main.config.timeout,13,400);
        g.drawString("System Exit: " + Main.config.sysExit,13,420);
        g.drawString("Max Bid Order (% of Target): " + Main.config.maxBidVol, 13, 440);
        g.drawString("Bidding: " + Main.bidding, 13,460);
        g.drawString("* Trades CSV output to console log" , 260, 360);
        g.drawString("* Bid prices are negative" , 260, 380);
        g.drawString("* Ask prices are post tax" , 260, 400);
        g.drawString("* Press pause + resume to toggle bidding" , 260, 420);
    }
}