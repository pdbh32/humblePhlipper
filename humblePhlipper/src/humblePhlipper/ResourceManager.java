package humblePhlipper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;

import static org.dreambot.core.Instance.getInstance;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/*

Classes Resources.API.* do NOT match the structure of their Resources.API json source files,
which are not in a particularly easy to manipulate structure. Instead, they
describe properties of an individual item (which DO match sources). We store
Resources.API data in Map<Integer, Resources.API.*>  maps where Integer is an item ID.

Classes Resources.SavedData.* DO match the structure of their local json source files.

*/

public class ResourceManager {
    public final Gson gson;
    public boolean updateError;
    public humblePhlipper.resources.Identity identity;
    public Map<Integer, humblePhlipper.resources.wikiObject.Mapping> mappingMap;
    public Map<Integer, humblePhlipper.resources.wikiObject.Latest> latestMap;
    public Map<Integer, humblePhlipper.resources.wikiObject.FiveMinute> fiveMinuteMap;
    public Map<Integer, humblePhlipper.resources.wikiObject.OneHour> oneHourMap;
    private ScheduledExecutorService latestApiScheduler;
    private ScheduledExecutorService fiveMinuteApiScheduler;
    private ScheduledExecutorService oneHourApiScheduler;
    public humblePhlipper.resources.data.FourHourLimits fourHourLimits;
    public humblePhlipper.resources.data.Config config;

    public humblePhlipper.resources.Items items;
    public humblePhlipper.resources.Session session;

    private ScheduledExecutorService webhookScheduler;
    private ScheduledExecutorService noSelfCompeting;

    public ResourceManager() {
        this.gson = new GsonBuilder().serializeNulls().create();
        this.updateError = false;

        // (0) Load identity,
        loadIdentity();

        // (1) Set API maps and thread,
        try {
            updateMappingMap(); // We only need to set this once
            updateLatestMap();
            updateFiveMinuteMap();
            updateOneHourMap();
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                new humblePhlipper.ErrorModal(this);
            });
        }

        // (2) Load and set four hour limits and set default config,
        loadFourHourLimits();
        this.config = new humblePhlipper.resources.data.Config(); // Custom config is loaded and set by GUI/CLI

        // (3) Initialise items
        this.items = new humblePhlipper.resources.Items(this);

        // (4) Initialise session
        this.session = new humblePhlipper.resources.Session();

        // (5) Set Discord post scheduler
        setWebhookScheduler();

        // (6) Set noCompetition scheduler
        setNoSelfCompeting();
    }

    private void setWebhookScheduler() {
        webhookScheduler = Executors.newSingleThreadScheduledExecutor();
        webhookScheduler.scheduleAtFixedRate(() -> {
            if (config.getDiscordWebhook() == null && getInstance().getDiscordWebhook() == null) {
                return;
            }
            int identifier = new Random().nextInt(100000);
            if (config.getDebug()) { Logger.log("<WEBHOOK " + identifier + ">"); }
            humblePhlipper.network.webhook.ClientProtocol cp = new humblePhlipper.network.webhook.ClientProtocol(this);
            humblePhlipper.network.webhook.ServerProtocol sp = new humblePhlipper.network.webhook.ServerProtocol(this);
            Executors.newCachedThreadPool().submit(() -> {
                new humblePhlipper.network.Client(1969, cp, sp, identifier);
                if (config.getDebug()) { Logger.log("</WEBHOOK " + identifier + ">"); }
            });
        }, initialDelay(3600), 3600, TimeUnit.SECONDS);
    }

    private void setNoSelfCompeting() {
        noSelfCompeting = Executors.newSingleThreadScheduledExecutor();
        noSelfCompeting.scheduleAtFixedRate(() -> {
            if (!config.getNoSelfCompeting()) {
                return;
            }
            int identifier = new Random().nextInt(100000);
            if (config.getDebug()) { Logger.log("<NOSELFCOMPETING " + identifier + ">"); }
            humblePhlipper.network.noSelfCompeting.ClientProtocol cp = new humblePhlipper.network.noSelfCompeting.ClientProtocol(this);
            humblePhlipper.network.noSelfCompeting.ServerProtocol sp = new humblePhlipper.network.noSelfCompeting.ServerProtocol(this);
            Executors.newCachedThreadPool().submit(() -> {
                new humblePhlipper.network.Client(2001, cp, sp, identifier);
                if (config.getDebug()) { Logger.log("</NOSELFCOMPETING " + identifier + ">"); }
            });
        }, initialDelay(10), 10, TimeUnit.SECONDS);
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
            int identifier = new Random().nextInt(100000);
            if (config.getDebug()) { Logger.log("<LATEST " + identifier + ">"); }
            humblePhlipper.network.wikiData.ClientProtocol cp = new humblePhlipper.network.wikiData.ClientProtocol(this, humblePhlipper.network.wikiData.Request.LATEST);
            humblePhlipper.network.wikiData.ServerProtocol sp = new humblePhlipper.network.wikiData.ServerProtocol(this, humblePhlipper.network.wikiData.Request.LATEST);
            Executors.newCachedThreadPool().submit(() -> {
                new humblePhlipper.network.Client(1066, cp, sp, identifier);
                items.updateAllLatest();
                items.updateAllPricing();
                if (config.getDebug()) { Logger.log("</LATEST " + identifier + ">"); }
            });
        }, initialDelay(10 + identity.deterministicInt(10)),10 + identity.deterministicInt(10), TimeUnit.SECONDS);
    }

    private void setFiveMinuteApiScheduler() {
        fiveMinuteApiScheduler = Executors.newSingleThreadScheduledExecutor();
        fiveMinuteApiScheduler.scheduleAtFixedRate(() -> {
            int identifier = new Random().nextInt(100000);
            if (config.getDebug()) { Logger.log("<FIVEMINUTE " + identifier + ">"); }
            humblePhlipper.network.wikiData.ClientProtocol cp = new humblePhlipper.network.wikiData.ClientProtocol(this, humblePhlipper.network.wikiData.Request.FIVEMINUTE);
            humblePhlipper.network.wikiData.ServerProtocol sp = new humblePhlipper.network.wikiData.ServerProtocol(this, humblePhlipper.network.wikiData.Request.FIVEMINUTE);
            Executors.newCachedThreadPool().submit(() -> {
                new humblePhlipper.network.Client(1776, cp, sp, identifier);
                items.updateAllFiveMinute();
                items.updateAllPricing();
                if (config.getDebug()) { Logger.log("</FIVEMINUTE " + identifier + ">"); }
            });
        }, initialDelay(300), 300, TimeUnit.SECONDS);
    }

    private void setOneHourApiScheduler() {
        oneHourApiScheduler = Executors.newSingleThreadScheduledExecutor();
        oneHourApiScheduler.scheduleAtFixedRate(() -> {
            int identifier = new Random().nextInt(100000);
            if (config.getDebug()) { Logger.log("<ONEHOUR " + identifier + ">"); }
            humblePhlipper.network.wikiData.ClientProtocol cp = new humblePhlipper.network.wikiData.ClientProtocol(this, humblePhlipper.network.wikiData.Request.ONEHOUR);
            humblePhlipper.network.wikiData.ServerProtocol sp = new humblePhlipper.network.wikiData.ServerProtocol(this, humblePhlipper.network.wikiData.Request.ONEHOUR);
            Executors.newCachedThreadPool().submit(() -> {
                new humblePhlipper.network.Client(1929, cp, sp, identifier);
                items.updateAllOneHour();
                items.updateAllPricing();
                if (config.getDebug()) { Logger.log("</ONEHOUR " + identifier + ">"); }
            });
        }, initialDelay(3600), 3600, TimeUnit.SECONDS);
    }

    private long initialDelay(int interval) {
        long currentTime = Instant.now().getEpochSecond();
        long nextUpdateTime = ((currentTime / interval) + 1) * interval + 15 + identity.deterministicInt(30); // 15-45 second delay as a randomised precaution
        return nextUpdateTime - currentTime;
    }

    public void disposeSchedulers() {
        disposeWebhookScheduler();
        disposeNoSelfCompeting();
        disposeLatestApiScheduler();
        disposeFiveMinuteApiScheduler();
        disposeOneHourApiScheduler();
    }

    private void disposeWebhookScheduler() {
        if (webhookScheduler == null) { return; }
        webhookScheduler.shutdownNow();
    }

    private void disposeNoSelfCompeting() {
        if (noSelfCompeting == null) { return; }
        noSelfCompeting.shutdownNow();
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

    private void updateMappingMap() throws Exception { updateMap("mapping");}

    public void updateLatestMap() throws Exception { updateMap("latest"); }

    public void updateFiveMinuteMap() throws Exception { updateMap("5m"); }

    public void updateOneHourMap() throws Exception { updateMap("1h"); }

    private void updateMap(String urlRoute) throws Exception {
        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/" + urlRoute);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            for (Map.Entry<String, List<String>> entry : identity.requestHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    connection.addRequestProperty(entry.getKey(), value);
                }
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                switch (urlRoute) {
                    case "mapping":
                        List<humblePhlipper.resources.wikiObject.Mapping> mappingList = gson.fromJson(reader, new TypeToken<List<humblePhlipper.resources.wikiObject.Mapping>>() {
                        }.getType());
                        mappingMap = mappingList.stream().collect(Collectors.toMap(humblePhlipper.resources.wikiObject.Mapping::getId, Function.identity()));
                        break;
                    case "latest":
                        Map<String, Map<Integer, humblePhlipper.resources.wikiObject.Latest>> latestMapData = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, humblePhlipper.resources.wikiObject.Latest>>>() {
                        }.getType());
                        latestMap = latestMapData.get("data");
                        break;
                    case "5m":
                        Map fiveMinuteMapData = gson.fromJson(reader, Map.class);
                        String fiveMinuteJson = gson.toJson(fiveMinuteMapData.get("data"));
                        fiveMinuteMap = gson.fromJson(fiveMinuteJson, new TypeToken<Map<Integer, humblePhlipper.resources.wikiObject.FiveMinute>>() {
                        }.getType());
                        break;
                    case "1h":
                        Map oneHourMapData = gson.fromJson(reader, Map.class);
                        String oneHourJson = gson.toJson(oneHourMapData.get("data"));
                        oneHourMap = gson.fromJson(oneHourJson, new TypeToken<Map<Integer, humblePhlipper.resources.wikiObject.OneHour>>() {
                        }.getType());
                        break;
                }
                updateError = false;
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("<ERROR>");
            System.err.println("Error fetching data from Wiki!\nYou may need to change the request-headers in the file\nDreamBot/Scripts/humblePhlipper/Identity.json");
            Logger.log("</ERROR>");
            updateError = true;
            throw e;
        }
    }
    private void loadIdentity() {
        identity = ScriptSettings.load(humblePhlipper.resources.Identity.class, "humblePhlipper", "Identity.json");
        if (identity.uuid == null) {
            identity.uuid = UUID.randomUUID();
            saveIdentity();
        }
        if (identity.requestHeaders == null) {
            identity.requestHeaders = new HashMap<>();
            identity.requestHeaders.put("User-Agent", Collections.singletonList("humblePhlipper " + identity.uuid.toString()));
            saveIdentity();
        }
        else if (identity.requestHeaders.get("User-Agent") == null) {
            identity.requestHeaders.put("User-Agent", Collections.singletonList("humblePhlipper " + identity.uuid.toString()));
            saveIdentity();
        }
    }

    public void saveIdentity() {
        ScriptSettings.save(identity, "humblePhlipper", "Identity.json");
    }

    public void loadFourHourLimits() {
        String file = AccountManager.getAccountHash().replaceAll("[^a-zA-Z0-9]", "") + ".json";
        fourHourLimits = ScriptSettings.load(humblePhlipper.resources.data.FourHourLimits.class, "humblePhlipper", "FourHourLimits", file);

        // If not empty, update refresh times and return, else, set default,
        if (!fourHourLimits.isEmpty()) {
            updateFourHourLimits();
            return;
        }
        for (Integer ID : mappingMap.keySet()) {
            fourHourLimits.put(ID, new humblePhlipper.resources.data.FourHourLimits.FourHourLimit());
        }
    }

    public void saveFourHourLimits() {
        if (AccountManager.getAccountHash().equals("")) {
            return;
        }
        String file = AccountManager.getAccountHash().replaceAll("[^a-zA-Z0-9]", "") + ".json";
        ScriptSettings.save(fourHourLimits, "humblePhlipper", "FourHourLimits", file);
    }

    public void updateFourHourLimits() {
        for (Integer ID : fourHourLimits.keySet()) {
            if (fourHourLimits.get(ID).getCountdownMinutes() < 0) {
                fourHourLimits.get(ID).setUsedLimit(0);
            }
        }
    }

    public void loadConfig(String fileName) {
        //String fileName = fileName + ".json";
        config = ScriptSettings.load(humblePhlipper.resources.data.Config.class, "humblePhlipper", "Config", fileName);
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
            config = gson.fromJson(params[0], humblePhlipper.resources.data.Config.class);
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
            humblePhlipper.resources.wikiObject.Mapping mapping = mappingMap.get(Integer.parseInt(input));
            if (mapping != null) {
                return mapping.getId();
            }
        } catch (NumberFormatException e) {
            for (humblePhlipper.resources.wikiObject.Mapping mapping : mappingMap.values()) {
                if (mapping.getName().equalsIgnoreCase(input)) {
                    return mapping.getId();
                }
            }
        }
        return -1;
    }

}