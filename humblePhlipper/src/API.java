import com.google.gson.Gson;
import org.dreambot.api.utilities.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class API {
    private final Gson gson = new Gson();
    public Map<Integer, Latest.Item> latestMap;

    public Map<Integer, Mapping> mappingMap;

    public API() {
        updateLatest();
        updateMapping();
    }

    public void updateLatest() {
        String jsonString = GetJSON("https://prices.runescape.wiki/api/v1/osrs/latest");
        Latest latestList = gson.fromJson(jsonString, Latest.class);
        latestMap = new HashMap<>();
        for (Map.Entry<String, Latest.Item> entry : latestList.getData().entrySet()) {
            String itemId = entry.getKey();
            Latest.Item item = entry.getValue();
            int itemIdInt = Integer.parseInt(itemId);
            latestMap.put(itemIdInt, item);
        }
    }

    public void updateMapping() {
        String jsonString = GetJSON("https://prices.runescape.wiki/api/v1/osrs/mapping");
        Mapping[] mappingList = gson.fromJson(jsonString, Mapping[].class);
        mappingMap = new HashMap<>();
        for (Mapping mapping : mappingList) {
            mappingMap.put(mapping.getId(), mapping);
        }

    }

    private static String GetJSON(String url) {
        try {
            // Set the API
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // Set the request method
            connection.setRequestMethod("GET");

            // Set the header
            connection.setRequestProperty("User-Agent", "f2p_flipper");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response from the API
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response as a string
                String jsonString = response.toString();
                return jsonString;
            } else {
                Logger.log("Error: HTTP request failed with response code " + responseCode);
            }
        } catch (IOException e) {
            Logger.log(e);
        }
        return null;
    }

    public static class Latest {
        private Map<String, Item> data;

        public Map<String, Item> getData() {
            return data;
        }

        public void setData(Map<String, Item> data) {
            this.data = data;
        }

        public static class Item extends Latest {
            private int high;
            private long highTime;
            private int low;
            private long lowTime;

            public int getHigh() {
                return high;
            }

            public void setHigh(int high) {
                this.high = high;
            }

            public long getHighTime() {
                return highTime;
            }

            public void setHighTime(long highTime) {
                this.highTime = highTime;
            }

            public int getLow() {
                return low;
            }

            public void setLow(int low) {
                this.low = low;
            }

            public long getLowTime() {
                return lowTime;
            }

            public void setLowTime(long lowTime) {
                this.lowTime = lowTime;
            }
        }
    }

    public static class Mapping {
        private String examine;
        private int id;
        private boolean members;
        private int lowalch;
        private int limit;
        private int value;
        private int highalch;
        private String icon;
        private String name;

        // Getters and setters for each field

        public String getExamine() {
            return examine;
        }

        public void setExamine(String examine) {
            this.examine = examine;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean getMembers() {
            return members;
        }

        public void setMembers(boolean members) {
            this.members = members;
        }

        public int getLowalch() {
            return lowalch;
        }

        public void setLowalch(int lowalch) {
            this.lowalch = lowalch;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getHighalch() {
            return highalch;
        }

        public void setHighalch(int highalch) {
            this.highalch = highalch;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public int getIdFromString(String input) {
        try {
            API.Mapping mapping = Main.api.mappingMap.get(Integer.parseInt(input));
            if (mapping != null) {
                return mapping.getId();
            }
        } catch (NumberFormatException e) {
            for (API.Mapping mapping : Main.api.mappingMap.values()) {
                if (mapping.getName().equalsIgnoreCase(input)) {
                    return mapping.getId();
                }
            }
        }
        return -1;
    }
}
