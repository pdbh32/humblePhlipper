// ResourceManager.java

package humblePhlipper;

import Gelox_.DiscordWebhook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.AccountManager;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.dreambot.core.Instance.getInstance;

/*

Classes Resources.API.* do NOT match the structure of their Resources.API json source files,
which are not in a particularly easy to manipulate structure. Instead, they
describe properties of an individual item (which DO match sources). We store
Resources.API data in Map<Integer, Resources.API.*>  maps where Integer is an item ID.

Classes Resources.SavedData.* DO match the structure of their local json source files.

*/

public class ResourceManager {
    public final Gson gson;
    public Map<Integer, humblePhlipper.resources.api.Mapping> mappingMap;
    public Map<Integer, humblePhlipper.resources.api.Latest> latestMap;
    public Map<Integer, humblePhlipper.resources.api.FiveMinute> fiveMinuteMap;
    public Map<Integer, humblePhlipper.resources.api.OneHour> oneHourMap;
    private ScheduledExecutorService latestApiScheduler;
    private ScheduledExecutorService fiveMinuteApiScheduler;
    private ScheduledExecutorService oneHourApiScheduler;
    public humblePhlipper.resources.savedData.FourHourLimits fourHourLimits;
    public humblePhlipper.resources.savedData.Config config;

    public humblePhlipper.resources.Items items;
    public humblePhlipper.resources.Session session;

    private ScheduledExecutorService webhookScheduler;

    public ResourceManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // (1) Set API maps and thread,
        updateMappingMap(); // We only need to set this once
        updateLatestMap();
        updateFiveMinuteMap();
        updateOneHourMap();

        // (2) Load and set four hour limits and set default config,
        loadFourHourLimits();
        this.config = new humblePhlipper.resources.savedData.Config(); // Custom config is loaded and set by GUI3/CLI

        // (3) Initialise items
        this.items = new humblePhlipper.resources.Items(this);

        // (4) Initialise session
        this.session = new humblePhlipper.resources.Session();

        // (5) Set Discord post scheduler
        setWebhookScheduler();
    }

    private void setWebhookScheduler() {
        webhookScheduler = Executors.newSingleThreadScheduledExecutor();
        webhookScheduler.scheduleAtFixedRate(() -> {
            if (getInstance().getDiscordWebhook() != null) {
                DiscordWebhook webhook = new DiscordWebhook(getInstance().getDiscordWebhook());
                webhook.setContent("Account: " + (AccountManager.getAccountHash()).substring(0,6) + "..., Profit: " + Math.round(session.getProfit()));
                webhook.setAvatarUrl("https://i.postimg.cc/W4DLDmhP/humble-Phlipper.png");
                webhook.setUsername("humblePhlipper");
                webhook.setTts(true);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .addField("Profit", String.valueOf(Math.round(session.getProfit())), false)
                        .addField("Runtime", session.getTimer().formatTime(), false)
                        .addField("Profit/Hr", String.valueOf(Math.round(3600000 * session.getProfit() / session.getTimer().elapsed())), false)
                        .addField("Timeout", String.valueOf(config.getTimeout()), false));
                try { webhook.execute(); }
                catch(Exception ignored) {}
            }
        }, 60, 60, TimeUnit.MINUTES);
    }

    public void setApiSchedulers() {
        setOneHourApiScheduler(); // we need this for volume irrespective of pricing
        switch (config.getPricing()) {
            case "latest":
                setLatestApiScheduler();
                break;
            case "fiveMinute":
                setFiveMinuteApiScheduler();
                break;
            case "bestOfLatestFiveMinute":
            case "worstOfLatestFiveMinute":
                setLatestApiScheduler();
                setFiveMinuteApiScheduler();
                break;
        }
    }

    private void setLatestApiScheduler() {
        latestApiScheduler = Executors.newSingleThreadScheduledExecutor();
        latestApiScheduler.scheduleAtFixedRate(() -> {
            updateLatestMap();
            items.updateAllLatest();
            items.updateAllPricing();
        }, config.getApiInterval(), config.getApiInterval(), TimeUnit.SECONDS);
    }

    private void setFiveMinuteApiScheduler() {
        fiveMinuteApiScheduler = Executors.newSingleThreadScheduledExecutor();
        fiveMinuteApiScheduler.scheduleAtFixedRate(() -> {
            updateFiveMinuteMap();
            items.updateAllFiveMinute();
            items.updateAllPricing();
        }, initialDelay(300), 300, TimeUnit.SECONDS);
    }

    private void setOneHourApiScheduler() {
        oneHourApiScheduler = Executors.newSingleThreadScheduledExecutor();
        oneHourApiScheduler.scheduleAtFixedRate(() -> {
            updateOneHourMap();
            items.updateAllOneHour();
            items.updateAllPricing();
        }, initialDelay(3600), 3600, TimeUnit.SECONDS);
    }

    private long initialDelay(int interval) {
        long currentTime = Instant.now().getEpochSecond();
        long nextUpdateTime = ((currentTime / interval) + 1) * interval + 3; // 3 second delay as a precaution
        return nextUpdateTime - currentTime;
    }

    public void disposeWebhookScheduler() {
        if (webhookScheduler == null) { return; }
        webhookScheduler.shutdownNow();
    }

    public void disposeApiSchedulers() {
        disposeLatestApiScheduler();
        disposeFiveMinuteApiScheduler();
        disposeOneHourApiScheduler();
    }
    private void disposeLatestApiScheduler() {
        if (latestApiScheduler == null) { return; }
        latestApiScheduler.shutdownNow();
    }
    private void disposeFiveMinuteApiScheduler() {
        if (fiveMinuteApiScheduler == null) { return; }
        fiveMinuteApiScheduler.shutdownNow();
    }
    private void disposeOneHourApiScheduler() {
        if (oneHourApiScheduler == null) { return; }
        oneHourApiScheduler.shutdownNow();
    }

    private void updateMappingMap() {
        updateMap("mapping");
    }

    private void updateLatestMap() {
        updateMap("latest");
    }

    private void updateFiveMinuteMap() {
        updateMap("5m");
    }

    private void updateOneHourMap() {
        updateMap("1h");
    }

    private void updateMap(String urlRoute) {
        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/" + urlRoute);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "f2p_flipper");

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                switch (urlRoute) {
                    case "mapping":
                        List<humblePhlipper.resources.api.Mapping> mappingList = gson.fromJson(reader, new TypeToken<List<humblePhlipper.resources.api.Mapping>>() {}.getType());
                        mappingMap = mappingList.stream().collect(Collectors.toMap(humblePhlipper.resources.api.Mapping::getId, Function.identity()));
                        break;
                    case "latest":
                        Map<String, Map<Integer, humblePhlipper.resources.api.Latest>> latestMapData = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, humblePhlipper.resources.api.Latest>>>() {}.getType());
                        latestMap = latestMapData.get("data");
                        break;
                    case "5m":
                        Map fiveMinuteMapData = gson.fromJson(reader, Map.class);
                        String fiveMinuteJson = gson.toJson(fiveMinuteMapData.get("data"));
                        fiveMinuteMap = gson.fromJson(fiveMinuteJson, new TypeToken<Map<Integer, humblePhlipper.resources.api.FiveMinute>>() {}.getType());
                        break;
                    case "1h":
                        Map oneHourMapData = gson.fromJson(reader, Map.class);
                        String oneHourJson = gson.toJson(oneHourMapData.get("data"));
                        oneHourMap = gson.fromJson(oneHourJson, new TypeToken<Map<Integer, humblePhlipper.resources.api.OneHour>>() {}.getType());
                        break;
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFourHourLimits() {
        String file = AccountManager.getAccountHash().replaceAll("[^a-zA-Z0-9]", "") + ".json";
        fourHourLimits = ScriptSettings.load(humblePhlipper.resources.savedData.FourHourLimits.class, "humblePhlipper", "FourHourLimits", file);

        // If not empty, update refresh times and return, else set default,
        if (!fourHourLimits.isEmpty()) {
            updateFourHourLimits();
            return;
        }
        for (Integer ID : mappingMap.keySet()) {
            fourHourLimits.put(ID, new humblePhlipper.resources.savedData.FourHourLimits.FourHourLimit());
        }
    }

    public void saveFourHourLimits() {
        if (AccountManager.getAccountHash() == "") {
            return;
        }
        String file = AccountManager.getAccountHash().replaceAll("[^a-zA-Z0-9]", "") + ".json";
        ScriptSettings.save(fourHourLimits, "humblePhlipper", "FourHourLimits", file);
    }

    public void updateFourHourLimits() {
        for (Integer ID : fourHourLimits.keySet()) {
            if (Duration.between(fourHourLimits.get(ID).getRefreshTime(), LocalDateTime.now()).toMinutes() > 240) {
                fourHourLimits.get(ID).setUsedLimit(0);
            }
        }
    }

    public void loadConfig(String fileName) {
        //String fileName = fileName + ".json";
        config = ScriptSettings.load(humblePhlipper.resources.savedData.Config.class, "humblePhlipper", "Config", fileName);
    }

    public void saveConfig(String fileName) {
        //String file = fileName + ".json";
        ScriptSettings.save(config, "humblePhlipper", "Config", fileName);
    }

    public void setQuickStartConfig(String[] params) {
        if (params == null || params.length != 1) {
            return;
        }
        if (params[0].startsWith("<") && params[0].endsWith(">")) {
            loadConfig(params[0].substring(1, params[0].length() - 1));
        } else {
            config = gson.fromJson(params[0], humblePhlipper.resources.savedData.Config.class);
        }
    }

    public String getConfigString() {
        return gson.toJson(config);
    }

    public void setSelectionCSV() {
        String selectionsCSV = "name,bid,ask,bidVol,askVol,target";
        for (Integer ID : config.getSelections()) {
            selectionsCSV += "\n" + items.get(ID).getMapping().getName() + "," + items.get(ID).getBid() + "," + items.get(ID).getAsk() + "," + items.get(ID).getOneHour().getLowPriceVolume() + "," + items.get(ID).getOneHour().getHighPriceVolume() + "," + items.get(ID).getTargetVol();
        }
        session.incrementSessionHistory("selectionsCSV", selectionsCSV);
    }

    public int getIdFromString(String input) {
        try {
            humblePhlipper.resources.api.Mapping mapping = mappingMap.get(Integer.parseInt(input));
            if (mapping != null) {
                return mapping.getId();
            }
        } catch (NumberFormatException e) {
            for (humblePhlipper.resources.api.Mapping mapping : mappingMap.values()) {
                if (mapping.getName().equalsIgnoreCase(input)) {
                    return mapping.getId();
                }
            }
        }
        return -1;
    }

}