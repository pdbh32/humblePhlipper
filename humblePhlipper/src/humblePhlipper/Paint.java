package humblePhlipper;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

public class Paint {
    private final humblePhlipper.ResourceManager rm;
    private static final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private static final Color recBg = new Color(0, 0, 0, 127);
    public Paint(ResourceManager rm) {
        this.rm = rm;
    }

    public void onPaint(Graphics g) {
        // Define table row count
        int tableRowCount = Math.min((320 / 20), 1 + rm.config.getSelections().size());

        // Draw background rectangle for table (1)
        g.setColor(recBg);
        g.fillRect(7,7,757, tableRowCount *20);

        int tableX = 15;
        int tableY = 20;

        // Draw table headers
        g.setColor(Color.white);
        g.drawString("Name", tableX, tableY);
        g.drawString("Profit", tableX + 150 + 0*85, tableY);
        g.drawString("Sold", tableX + 150 + 1*85, tableY);
        g.drawString("Target", tableX + 150 + 2*85, tableY);
        g.drawString("Bid", tableX + 150 + 3*85, tableY);
        g.drawString("Margin", tableX + 150 + 4*85, tableY);
        g.drawString("1hr Vol", tableX + 150 + 5*85, tableY);
        g.drawString("4hr Refresh", tableX + 150 + 6*85, tableY);
        tableY += 20;

        for (Integer ID : rm.config.getSelections()) {
            humblePhlipper.resources.Items.Item item = rm.items.get(ID);
            if (tableY > 20 + 15*20) {
                break;
            }
            // Draw table rows
            g.drawString(item.getMapping().getName(), tableX, tableY);
            g.drawString(commaFormat.format(Math.round(item.getProfit())), tableX + 150 + 0*85, tableY);
            g.drawString(commaFormat.format(item.getSold()), tableX + 150 + 1*85, tableY);
            g.drawString(commaFormat.format(item.getTargetVol()), tableX + 150 + 2*85, tableY);
            g.drawString(commaFormat.format(item.getBid()), tableX + 150 + 3*85, tableY);
            g.drawString(commaFormat.format((int) Math.ceil(0.99 * item.getAsk() - item.getBid())), tableX + 150 + 4*85, tableY);
            g.drawString(commaFormat.format(item.getOneHour().getLowPriceVolume() + item.getOneHour().getHighPriceVolume()), tableX + 150 + 5*85, tableY);
            g.drawString((item.getFourHourLimit().getCountdownMinutes() < 0) ? "N/A" : (int) Math.ceil(item.getFourHourLimit().getCountdownMinutes()) + " mins", tableX + 150 + 6*85, tableY);

            tableY += 20;
        }

        g.setColor(recBg);
        g.fillRect(7,344,506,132);
        g.setColor(Color.WHITE);
        g.drawString("Profit: " + commaFormat.format(Math.round(rm.session.getProfit())) + " (" + commaFormat.format(Math.round(3600000 * rm.session.getProfit() /rm.session.getTimer().elapsed())) +"/hr)", 13, 360);
        g.drawString("Runtime: " + rm.session.getTimer().formatTime(), 13, 380);
        g.drawString("* Trades CSV output to console log" , 13, 400);
        g.drawString("* CSV bid prices are negative" , 13, 420);
        g.drawString("* CSV ask prices are post tax" , 13, 440);
        g.drawString("* Press pause + resume to toggle" , 13, 460);
        if (rm.session.getBidding()) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawString("bidding", 200,460);
        g.setColor(Color.WHITE);
        g.drawString("Timeout: " + rm.config.getTimeout() + " mins",260,360);
        g.drawString("Profit Cutoff: " + commaFormat.format(rm.config.getProfitCutOff()), 260,380);
        g.drawString("System Exit: " + rm.config.getSysExit(),260,400);
        g.drawString("Max Bid Value: " + commaFormat.format(rm.config.getMaxBidValue()), 260, 420);
        g.drawString("Max Bid Volume: " + commaFormat.format(rm.config.getMaxBidVol()), 260, 440);
        g.drawString("Pricing: " + rm.config.getPricing(), 260,460);

        // Finally, a cumulative profit graph
        g.setColor(recBg);
        g.fillRect(547,345,190,120);
        g.setColor(Color.WHITE);

        g.drawLine(567, 445, 567+150,445); // x-axis
        g.drawLine(567, 445, 567,365); // y-axis

        if (!rm.session.getTimeCumProfitMap().isEmpty()) {
            Long minX = rm.session.getTimeCumProfitMap().firstKey();
            Long maxX = rm.session.getTimeCumProfitMap().lastKey();
            Double minY = rm.session.getTimeCumProfitMap().values().stream().min(Double::compareTo).orElse(0.0);
            Double maxY = rm.session.getTimeCumProfitMap().values().stream().max(Double::compareTo).orElse(0.0);

            if (!maxY.equals(minY)) {
                final double y0 = (0 - minY) * 80 / (maxY - minY);
                g.drawLine(567, (int) (445 - y0), 567 + 150, (int) (445 - y0)); // y = 0 line

                g.drawString(commaFormat.format(Math.round((double) maxX /60000)) + "m", 697, 460);
                g.drawString(commaFormat.format(Math.round(minY)), 547 + 10, 460);
                g.drawString(commaFormat.format(Math.round(maxY)), 547 + 10, 360);

                // Map data values to the coordinate system and plot graph,
                int prevX = 567;
                int prevY = (int) (445 - y0);
                for (Map.Entry<Long, Double> entry : rm.session.getTimeCumProfitMap().entrySet()) {
                    Long xValue = entry.getKey();
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