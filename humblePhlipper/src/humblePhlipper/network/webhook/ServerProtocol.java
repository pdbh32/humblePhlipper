package humblePhlipper.network.webhook;

import Gelox_.DiscordWebhook;

import java.io.IOException;
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
    public void go() throws IOException {
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
        if (rm.config.getDebug()) {
            DiscordWebhook debugWebhook = new DiscordWebhook(discordWebhook);
            debugWebhook.setContent("humblePhlipperDebug");
            debugWebhook.setAvatarUrl("https://i.postimg.cc/YC9WRdnJ/tempDiag.png");
            debugWebhook.setUsername("humblePhlipperDebug");
            debugWebhook.setTts(false);
            debugWebhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .addField("Slot 0", humblePhlipper.dbGE.Slot.get(0).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(0).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(0).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(0).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(0).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 1", humblePhlipper.dbGE.Slot.get(1).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(1).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(1).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(1).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(1).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 2", humblePhlipper.dbGE.Slot.get(2).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(2).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(2).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(2).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(2).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 3", humblePhlipper.dbGE.Slot.get(3).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(3).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(3).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(3).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(3).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 4", humblePhlipper.dbGE.Slot.get(4).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(4).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(4).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(4).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(4).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 5", humblePhlipper.dbGE.Slot.get(5).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(5).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(5).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(5).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(5).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 6", humblePhlipper.dbGE.Slot.get(6).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(6).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(6).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(6).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(6).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false)
                    .addField("Slot 7", humblePhlipper.dbGE.Slot.get(7).getType() + "  /  " + ((humblePhlipper.dbGE.Slot.get(7).getItemId() != -1) ? rm.items.get(humblePhlipper.dbGE.Slot.get(7).getItemId()).getMapping().getName() + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(7).getItemId()).getBid()) + "  /  " + commaFormat.format(rm.items.get(humblePhlipper.dbGE.Slot.get(7).getItemId()).getAsk()) : "-1  /  -1;  /  -1"), false));
            try {
                debugWebhook.execute();
            } catch (Exception ignored) {}
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
