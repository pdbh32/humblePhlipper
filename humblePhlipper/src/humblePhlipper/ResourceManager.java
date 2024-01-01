// ResourceManager.java

package humblePhlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.dreambot.api.Client;
import org.dreambot.api.settings.ScriptSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final Gson gson;
    private final ScheduledExecutorService apiScheduler;
    public Map<Integer, humblePhlipper.Resources.API.Mapping> mappingMap;
    public Map<Integer, humblePhlipper.Resources.API.Latest> latestMap;
    public Map<Integer, humblePhlipper.Resources.API.FiveMinute> fiveMinuteMap;
    public Map<Integer, humblePhlipper.Resources.API.OneHour> oneHourMap;
    public humblePhlipper.Resources.SavedData.FourHourLimits fourHourLimits;
    public humblePhlipper.Resources.SavedData.Config config;

    public humblePhlipper.Resources.Items items;
    public humblePhlipper.Resources.Session session;

    public ResourceManager() {
        gson = new Gson();

        // (1) Set API maps and thread,
        updateMappingMap(); // We only need to set this once
        updateLatestMap();
        updateFiveMinuteMap();
        updateOneHourMap();

        // (2) Load and set four hour limits and set default config,
        loadFourHourLimits();
        this.config = new humblePhlipper.Resources.SavedData.Config(); // Custom config is loaded and set by GUI3/CLI

        // (3) Initialise items
        this.items = new humblePhlipper.Resources.Items(this);

        // (4) Set thread to update API data and Items.Item fields
        apiScheduler = Executors.newSingleThreadScheduledExecutor();
        apiScheduler.scheduleAtFixedRate(() -> {
            updateLatestMap();
            updateFiveMinuteMap();
            updateOneHourMap();
            items.updateAllLatest();
            items.updateAllFiveMinute();
            items.updateAllOneHour();
            items.updateAllPricing();
        }, 1, 1, TimeUnit.SECONDS);

        // (5) Initialise session
        this.session = new humblePhlipper.Resources.Session();
    }
    public void disposeApiScheduler() { apiScheduler.shutdownNow(); }

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
                        List<humblePhlipper.Resources.API.Mapping> mappingList = gson.fromJson(reader, new TypeToken<List<humblePhlipper.Resources.API.Mapping>>() {}.getType());
                        mappingMap = mappingList.stream().collect(Collectors.toMap(humblePhlipper.Resources.API.Mapping::getId, Function.identity()));
                        break;
                    case "latest":
                        Map<String, Map<Integer, humblePhlipper.Resources.API.Latest>> latestMapData = gson.fromJson(reader, new TypeToken<Map<String, Map<Integer, humblePhlipper.Resources.API.Latest>>>() {}.getType());
                        latestMap = latestMapData.get("data");
                        break;
                    case "5m":
                        Map fiveMinuteMapData = gson.fromJson(reader, Map.class);
                        String fiveMinuteJson = gson.toJson(fiveMinuteMapData.get("data"));
                        fiveMinuteMap = gson.fromJson(fiveMinuteJson, new TypeToken<Map<Integer, humblePhlipper.Resources.API.FiveMinute>>() {}.getType());
                        break;
                    case "1h":
                        Map oneHourMapData = gson.fromJson(reader, Map.class);
                        String oneHourJson = gson.toJson(oneHourMapData.get("data"));
                        oneHourMap = gson.fromJson(oneHourJson, new TypeToken<Map<Integer, humblePhlipper.Resources.API.OneHour>>() {}.getType());
                        break;
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFourHourLimits() {
        String file = Client.getUsername() + ".json";
        fourHourLimits = ScriptSettings.load(humblePhlipper.Resources.SavedData.FourHourLimits.class, "humblePhlipper", "FourHourLimits", file);

        // If not empty, update (refresh times) and return, else set default,
        if (!fourHourLimits.isEmpty()) {
            updateFourHourLimits();
            return;
        }
        for (Integer ID : mappingMap.keySet()) {
            fourHourLimits.put(ID, new humblePhlipper.Resources.SavedData.FourHourLimits.FourHourLimit());
        }

        // If it was empty because this is a new user (not because user hasn't logged in yet), save new file,
        if (Client.getUsername() != null && Client.getUsername() != "") {
            saveFourHourLimits();
        }
    }

    public void saveFourHourLimits() {
        String file = Client.getUsername() + ".json";
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
        String file = fileName + ".json";
        config = ScriptSettings.load(humblePhlipper.Resources.SavedData.Config.class, "humblePhlipper", "Config", file);
    }

    public void saveConfig(String fileName) {
        String file = fileName + ".json";
        ScriptSettings.save(config, "humblePhlipper", "Config", file);
    }

    public void setQuickStartConfig(String[] params) {
        if (params == null || params.length != 1) {
            return;
        }
        config = gson.fromJson(params[0], humblePhlipper.Resources.SavedData.Config.class);
    }

    public int getIdFromString(String input) {
        try {
            humblePhlipper.Resources.API.Mapping mapping = mappingMap.get(Integer.parseInt(input));
            if (mapping != null) {
                return mapping.getId();
            }
        } catch (NumberFormatException e) {
            for (humblePhlipper.Resources.API.Mapping mapping : mappingMap.values()) {
                if (mapping.getName().equalsIgnoreCase(input)) {
                    return mapping.getId();
                }
            }
        }
        return -1;
    }

}