import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Paint {

    public void onPaint(Graphics g) {
        // Create a copy of itemList
        List<Item> itemListCopy = new ArrayList<>(Main.config.itemList);

        // Sort itemListCopy by item.name (assuming item.name is a String)
        Collections.sort(itemListCopy, Comparator.comparing(item -> item.name));

        int x = 15;
        int y = 20;
        double profit = 0;

        // Draw table headers
        g.drawString("Item Name", x, y);
        g.drawString("Sold", x + 150, y);
        g.drawString("Target Vol", x + 250, y);
        g.drawString("Profit", x + 350, y);
        y += 20;

        for (Item item : itemListCopy) {
            // Draw table rows
            g.drawString(item.name, x, y);
            g.drawString(String.valueOf(item.sold), x + 150, y);
            g.drawString(String.valueOf(item.targetVol), x + 250, y);
            g.drawString(String.valueOf(Math.round(item.profit)), x + 350, y);

            // Update total profit
            profit += item.profit;
            y += 20;
        }

        g.setColor(Color.BLACK);
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