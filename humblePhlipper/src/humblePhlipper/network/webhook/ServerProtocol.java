package humblePhlipper.network.webhook;

import Gelox_.DiscordWebhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static org.dreambot.core.Instance.getInstance;

public class ServerProtocol extends humblePhlipper.network.Protocol {
    private final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private final Map<String, Object[]> accountUsernameMap = new HashMap<>();
    public ServerProtocol(humblePhlipper.ResourceManager rm) {
        super(rm);
        addData(new ClientMessage(rm));
    }
    @Override
    public void go(BufferedReader in, PrintWriter out) throws IOException {
        ClientMessage cm = new ClientMessage(in.readLine());
        addData(cm);
    }
    public void addData(ClientMessage cm) {
        accountUsernameMap.put(cm.accountUsername, new Object[]{cm.profit, cm.elapsed});
    }
    private int getInstances() {
        return accountUsernameMap.size();
    }
    private double getTotalProfit() {
        double totalProfit = 0;
        for (Object[] value : accountUsernameMap.values()) {
            totalProfit += (double) value[0];
        }
        return totalProfit;
    }

    private long getTotalElapsed() {
        long totalElapsed = 0;
        for (Object[] value : accountUsernameMap.values()) {
            totalElapsed += (long) value[1];
        }
        return totalElapsed;
    }

    private double getAvgElapsed() {
        return (double) getTotalElapsed() /getInstances();
    }

    private double getAvgProfitPerHour() {
        return 3600000 * getTotalProfit()/getTotalElapsed();
    }
    private double getTotalProfitPerHour() {
        return getAvgProfitPerHour() * getInstances();
    }
    @Override
    public void finish() {
        String discordWebhook;
        if (rm.config.getDiscordWebhook() != null) {
            discordWebhook = rm.config.getDiscordWebhook();
        } else if (getInstance().getDiscordWebhook() != null) {
            discordWebhook = getInstance().getDiscordWebhook();
        } else {
            return;
        }
        DiscordWebhook updateWebhook = new DiscordWebhook(discordWebhook);
        updateWebhook.setContent("Total Profit: " + commaFormat.format(Math.round(getTotalProfit())) + ", Avg Profit/Hr: " + commaFormat.format(Math.round(getAvgProfitPerHour())));
        updateWebhook.setAvatarUrl("https://i.postimg.cc/W4DLDmhP/humble-Phlipper.png");
        updateWebhook.setUsername("humblePhlipper");
        updateWebhook.setTts(false);
        updateWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                .addField("Connected Instances", String.valueOf(getInstances()), false)
                .addField("Total Profit", commaFormat.format(Math.round(getTotalProfit())), false)
                .addField("Total Profit/Hr", commaFormat.format(Math.round(getTotalProfitPerHour())), false)
                .addField("Avg Profit/Hr", commaFormat.format(Math.round(getAvgProfitPerHour())), false)
                .addField("Avg Runtime", commaFormat.format(Math.round(getAvgElapsed()/60000)) + " mins", false));
        try { updateWebhook.execute(); }
        catch(Exception ignored) {}
    }
}
