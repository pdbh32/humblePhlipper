//Paint.java

import java.awt.*;
import java.util.*;
import java.util.List;

public class Paint {
    private static final Color recBg = new Color(0, 0, 0, 127);
    public void onPaint(Graphics g) {
        // Create an itemList of the Item values in itemMap and sort alphabetically
        List<Item> itemList = new ArrayList<>(Main.getItemMap().values());
        Collections.sort(itemList, Comparator.comparing(item -> item.name));

        // Define table row count
        int tableRowCount = Math.min((320 / 20), 1 + itemList.size());

        // Draw background rectangle for table (1)
        g.setColor(recBg);
        g.fillRect(7,7,757, tableRowCount *20);

        int tableX = 15;
        int tableY = 20;
        double profit = 0;

        // Draw table headers
        g.setColor(Color.white);
        g.drawString("Item Name", tableX, tableY);
        g.drawString("Profit", tableX + 150, tableY);
        g.drawString("Bought", tableX + 150 + 90, tableY);
        g.drawString("Sold", tableX + 150 + 2*90, tableY);
        g.drawString("Target", tableX + 150 + 3*90, tableY);
        g.drawString("Bid", tableX + 150 + 4*90, tableY);
        g.drawString("Ask", tableX + 150 + 5*90, tableY);
        g.drawString("Margin", tableX + 150 + 6*90, tableY);
        tableY += 20;

        for (Item item : itemList) {
            if (tableY > 20 + 15*20) {
                break;
            }
            // Draw table rows
            g.drawString(item.name, tableX, tableY);
            g.drawString(String.valueOf(Math.round(item.profit)), tableX + 150, tableY);
            g.drawString(String.valueOf(item.bought), tableX + 150 + 90, tableY);
            g.drawString(String.valueOf(item.sold), tableX + 150 + 2*90, tableY);
            g.drawString(String.valueOf(item.targetVol), tableX + 150 + 3*90, tableY);
            g.drawString(String.valueOf(Main.api.latestMap.get(item.id).getLow()), tableX + 150 + 4*90, tableY);
            g.drawString(String.valueOf(Main.api.latestMap.get(item.id).getHigh()), tableX + 150 + 5*90, tableY);
            g.drawString(String.valueOf(Math.ceil(0.99*Main.api.latestMap.get(item.id).getHigh())-Main.api.latestMap.get(item.id).getLow()), tableX + 150 + 6*90, tableY);
            // Update total profit
            profit += item.profit;
            tableY += 20;
        }

        g.setColor(recBg);
        g.fillRect(7,344,506,132);
        g.setColor(Color.WHITE);
        g.drawString("Profit: " + Math.round(profit), 13, 360);
        g.drawString("Runtime: " + Main.timer.formatTime(), 13, 380);
        g.drawString("Timeout (Minutes): " + Main.getTimeout(),13,400);
        g.drawString("System Exit: " + Main.getSysExit(),13,420);
        g.drawString("Max Bid Order (% of Target): " + Main.getMaxBidVol(), 13, 440);
        g.drawString("Bidding: " + Main.bidding, 13,460);
        g.drawString("* Trades CSV output to console log" , 260, 360);
        g.drawString("* Bid prices are negative" , 260, 380);
        g.drawString("* Ask prices are post tax" , 260, 400);
        g.drawString("* Press pause + resume to toggle bidding" , 260, 420);


        // Finally, a cumulative profit graph
        g.setColor(recBg);
        g.fillRect(547,345,190,120);
        g.setColor(Color.WHITE);

        g.drawLine(567, 445, 567+150,445); // x-axis
        g.drawLine(567, 445, 567,365); // y-axis

        if (!Main.getTimeCumProfitMap().isEmpty()) {
            Double minX = Main.getTimeCumProfitMap().firstKey();
            Double maxX = Main.getTimeCumProfitMap().lastKey();
            Double minY = Main.getTimeCumProfitMap().values().stream().min(Double::compareTo).orElse(0.0);
            Double maxY = Main.getTimeCumProfitMap().values().stream().max(Double::compareTo).orElse(0.0);

            if (!maxY.equals(minY)) {
                final double y0 = (0 - minY) * 80 / (maxY - minY);
                g.drawLine(567, (int) (445 - y0), 567 + 150, (int) (445 - y0)); // y = 0 line

                g.drawString(Math.round(maxX) + "m", 697, 460);
                g.drawString(String.valueOf(Math.round(minY)), 547 + 10, 460);
                g.drawString(String.valueOf(Math.round(maxY)), 547 + 10, 360);

                // Map data values to the coordinate system and plot graph,
                int prevX = 567;
                int prevY = (int) (445 - y0);
                for (Map.Entry<Double, Double> entry : Main.getTimeCumProfitMap().entrySet()) {
                    Double xValue = entry.getKey();
                    Double yValue = entry.getValue();

                    int x = (int) (567 + ((xValue - minX) * 150 / (maxX - minX)));
                    int y = (int) (445 - ((yValue - minY) * 80 / (maxY - minY)));

                    if (y < prevY) {
                        g.setColor(Color.GREEN);
                    } else {
                        g.setColor(Color.RED);
                    }
                    g.drawLine(prevX, prevY, x, y);

                    prevX = x;
                    prevY = y;
                }
            }
        }
    }
}