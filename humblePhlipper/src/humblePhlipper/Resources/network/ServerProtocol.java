package humblePhlipper.resources.network;

import Gelox_.DiscordWebhook;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static org.dreambot.core.Instance.getInstance;

public class ServerProtocol {
    private final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private Map<String, Object[]> accountUsernameMap = new HashMap<>();
    public ServerProtocol() {
        addData(new ClientMessage());
    }

    public ClientMessage read(String message) {
        return new ClientMessage(message);
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
    public void sendWebhook() {
        if (getInstance().getDiscordWebhook() == null) {
            return;
        }
        DiscordWebhook webhook = new DiscordWebhook(getInstance().getDiscordWebhook());
        webhook.setContent("Total Profit: " + commaFormat.format(Math.round(getTotalProfit())) + ", Avg Profit/Hr: " + commaFormat.format(Math.round(getAvgProfitPerHour())));
        webhook.setAvatarUrl("https://i.postimg.cc/W4DLDmhP/humble-Phlipper.png");
        webhook.setUsername("humblePhlipper");
        webhook.setTts(false);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .addField("Connected Instances", String.valueOf(getInstances()), false)
                .addField("Total Profit", commaFormat.format(Math.round(getTotalProfit())), false)
                .addField("Total Profit/Hr", commaFormat.format(Math.round(getTotalProfitPerHour())), false)
                .addField("Avg Profit/Hr", commaFormat.format(Math.round(getAvgProfitPerHour())), false)
                .addField("Avg Runtime", commaFormat.format(Math.round(getAvgElapsed()/60000)) + " mins", false));
        try { webhook.execute(); }
        catch(Exception ignored) {}
    }
}
